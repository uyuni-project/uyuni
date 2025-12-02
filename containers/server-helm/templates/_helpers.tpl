{{- define "deployment.container.image" -}}
{{- $imageName := .name -}}
{{- $uri := (printf "%s/%s:%s" .global.Values.repository $imageName .global.Values.version) | default .global.Chart.AppVersion -}}
{{- if .global.Values.images -}}
{{- $image := (get .global.Values.images $imageName) -}}
{{- if $image -}}
{{- $uri = $image -}}
{{- end -}}
{{- end -}}
{{- $uri -}}
{{- end -}}

{{- define "uyuni.pvc" -}}
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: {{ .name }}
  namespace: "{{ .context.Release.Namespace }}"
spec:
{{- if .context.Values.storageClass }}
{{- if (eq "-" .context.Values.storageClass) }}
  storageClassName: ""
{{- else }}
  storageClassName: "{{ .context.Values.storageClass }}"
{{- end }}
{{- end }}
  accessModes:
{{ toYaml .context.Values.accessModes | indent 4 }}
  resources:
    requests:
{{- $volumes := .context.Values.volumes | default dict }}
{{- $volume := (get $volumes .name) | default dict }}
      storage: {{ $volume.size | default .size }}
{{- if .context.Values.matchPvByLabel }}
  selector:
    matchLabels:
      data: {{ .name }}
{{- end }}
---
{{- end -}}
