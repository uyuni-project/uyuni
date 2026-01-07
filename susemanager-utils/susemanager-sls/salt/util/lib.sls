{%- set _find_binary_cache = {} -%}

{%- macro find_binary(binary_name) -%}
    {# Caching mechanism #}
    {%- if binary_name in _find_binary_cache -%}
        
        {{- _find_binary_cache[binary_name] -}}

    {%- else -%}

        {%- set search_paths = [
            '/bin',
            '/sbin',
            '/usr/bin',
            '/usr/sbin',
            '/usr/local/bin',
            '/usr/local/sbin',
        ] -%}
        
        {%- set result = namespace(found='') -%}
        {%- for path in search_paths -%}
            {%- set candidate = path ~ '/' ~ binary_name -%}
            {%- if result.found == '' and salt['file.file_exists'](candidate) -%}
                {%- set result.found = candidate -%}
            {%- endif -%}
        {%- endfor -%}
        
        {%- if result.found != '' -%}
            {%- do _find_binary_cache.update({binary_name: result.found}) -%}
            {{- result.found -}}
        {%- else -%}
            {%- set error_msg = "ERROR: Binary '" ~ binary_name ~ "' not found in any standard path." -%}
            {%- do salt['test.exception'](error_msg) -%}
        {%- endif -%}
    {%- endif -%}
{%- endmacro -%}
