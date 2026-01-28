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

{{/* uyuni.pvc writes the manifest for a PVC. */}}
{{/*   "name", the volume name, */}}
{{/*   "volume", the volume config data dictionary of the volume, */}}
{{/*   "defaultSize", the default size of the volume in case the user messes it up in the values, */}}
{{/*   "accessMode", the access mode to use for the volume */}}
{{- define "uyuni.pvc" -}}
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: {{ .name }}
  namespace: "{{ .root.Release.Namespace }}"
  labels:
    app.kubernetes.io/part-of: uyuni
{{- if .volume.extraLabels }}
    {{- toYaml .volume.extraLabels | nindent 4 }}
{{- end }}
{{- if .volume.annotations }}
  annotations: {{ toYaml .volume.annotations | nindent 4 }}
{{- end }}
spec:
{{- if .volume.storageClass }}
{{- if (eq "-" .volume.storageClass) }}
  storageClassName: ""
{{- else }}
  storageClassName: "{{ .volume.storageClass }}"
{{- end }}
{{- end }}
  accessModes:
  - {{ .accessMode }}
  resources:
    requests:
      storage: {{ .volume.size | default .defaultSize }}
{{- if .volume.volumeName }}
  volumeName: "{{ .volume.volumeName }}"
{{- end }}
{{- if .volume.selector }}
  selector: {{ toYaml .volume.selector | nindent 4 }}
{{- end }}
---
{{- end -}}
