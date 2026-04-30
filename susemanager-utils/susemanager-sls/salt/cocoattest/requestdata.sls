include:

{% for result_type in salt['pillar.get']('attestation_data:result_types', []) %}

{%- if result_type  == 'SECURE_BOOT' %}
 - .coco_secure_boot
{%- endif %}

{%- if result_type  == 'SEV_SNP' %}
 - .coco_sev_snp
{%- endif %}

{%- if result_type  == 'IBM_PVATTEST' %}
 - .coco_ibm_pvattest
{%- endif %}


{% endfor %}
