/tmp/test_user_defined_state:
  file:
    - managed
    - contents:
      - This is a nice file.
    - user: root
    - group: root
    - mode: 644
