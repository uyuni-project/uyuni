{{/* uyuni.image computes the image URL out of the global registry and tag as well as overridden values. */}}
{{/*   "name", the image name */}}
{{/*   "global", the root object */}}
{{/*   "local", the configuration object containing the image and tag to override with */}}
{{- define "uyuni.image" -}}
{{- $tag := .global.Values.tag -}}
{{- if .local.tag -}}
{{- $tag = .local.tag -}}
{{- end -}}
{{- $uri := (printf "%s/%s:%s" .global.Values.repository .name $tag) -}}
{{- if .local.image -}}
{{- $uri = (printf "%s:%s" .local.image $tag) -}}
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
  namespace: {{ .root.Release.Namespace | quote }}
  labels:
    app.kubernetes.io/part-of: uyuni
{{- if .volume.extraLabels }}
{{ toYaml .volume.extraLabels | nindent 4 }}
{{- end }}
{{- if .volume.annotations }}
  annotations:
{{ toYaml .volume.annotations | nindent 4 }}
{{- end }}
spec:
{{- if .volume.storageClass }}
  {{- if eq "-" .volume.storageClass }}
  storageClassName: ""
  {{- else }}
  storageClassName: {{ .volume.storageClass | quote }}
  {{- end }}
{{- end }}
  accessModes:
    - {{ .accessMode }}
  resources:
    requests:
      storage: {{ .volume.size | default .defaultSize | quote }}
{{- if .volume.volumeName }}
  volumeName: {{ .volume.volumeName | quote }}
{{- end }}
{{- if .volume.selector }}
  selector:
{{ toYaml .volume.selector | nindent 4 }}
{{- end }}
---
{{- end -}}

{{- define "uyuni.pgPVCName" -}}
var-pgsql18
{{- end -}}
