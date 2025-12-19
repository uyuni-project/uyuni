--
-- Copyright (c) 2025 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE OR REPLACE FUNCTION search_namespace(
    search_term text
)
RETURNS TABLE (
    id bigint,
    namespace text,
    access_mode character(1),
    description text,
    rank real
)
AS $$
DECLARE
    sanitized text;
    tsquery_term tsquery;
    similarity_threshold real := 0.2;
    result_limit integer := 25;
    similarity_weight real := 0.5;
BEGIN
    sanitized := btrim(
        regexp_replace(
            lower(regexp_replace(search_term, '[^a-zA-Z0-9\.]', ' ', 'g')),  -- non-alphanum → space
            '[\.]+|\s+', ' ', 'g'  -- collapse multiple delim into one
        )
    );

    IF length(sanitized) >= 3 THEN
        tsquery_term := to_tsquery(
            'english',
            array_to_string(string_to_array(sanitized, ' '), ':* & ') || ':*'
        );
    ELSE
        tsquery_term := NULL;
    END IF;

    RETURN QUERY
    SELECT
        n.id,
        n.namespace::text,
        n.access_mode,
        n.description::text,
        CASE
            WHEN length(sanitized) >= 3 THEN
                ts_rank(
                    to_tsvector('english', row_data.clean_namespace || ' ' || coalesce(n.description, '')),
                    tsquery_term
                )
                + greatest(row_data.namespace_similarity, row_data.description_similarity) * similarity_weight
            ELSE
                greatest(row_data.namespace_similarity, row_data.description_similarity)
        END AS rank
    FROM access.namespace n
    CROSS JOIN LATERAL (
        SELECT
            lower(regexp_replace(n.namespace, '\.', ' ', 'g')) AS clean_namespace,
            similarity(lower(regexp_replace(n.namespace, '\.', ' ', 'g')), sanitized) AS namespace_similarity,
            similarity(lower(n.description), sanitized) AS description_similarity
    ) AS row_data
    WHERE
        (length(sanitized) >= 3 AND
            (
                tsquery_term IS NOT NULL AND
                to_tsvector('english', row_data.clean_namespace || ' ' || coalesce(n.description, '')) @@ tsquery_term
                OR row_data.namespace_similarity > similarity_threshold
                OR row_data.description_similarity > similarity_threshold
            )
        )
        OR
        (length(sanitized) < 3 AND
            (
                row_data.namespace_similarity > similarity_threshold
                OR row_data.description_similarity > similarity_threshold
            )
        )
    ORDER BY rank DESC
    LIMIT result_limit;
END;
$$
LANGUAGE plpgsql
STABLE;
