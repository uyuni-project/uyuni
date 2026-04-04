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

{{/* uyuni.nodePlacement writes the node placement properties for a pod */}}
{{/*   "global": the global placement configuration data */}}
{{/*   "local": the overwriting configurantion data */}}
{{- define "uyuni.nodePlacement" -}}
{{- $globalNode := .global | default (dict) -}}
{{- $localNode := .local | default (dict) -}}

{{- /* Handle nodeSelector */ -}}
{{- $nodeSelector := $globalNode.nodeSelector -}}
{{- if $localNode.nodeSelector }}{{ $nodeSelector = $localNode.nodeSelector }}{{ end -}}
{{- if $nodeSelector }}
nodeSelector:
{{- toYaml $nodeSelector | nindent 2 }}
{{- end }}

{{- /* Handle affinity */ -}}
{{- $affinity := $globalNode.affinity -}}
{{- if $localNode.affinity }}{{ $affinity = $localNode.affinity }}{{ end -}}
{{- if $affinity }}
affinity:
{{- toYaml $affinity | nindent 2 }}
{{- end }}

{{- /* Handle tolerations */ -}}
{{- $tolerations := $globalNode.tolerations -}}
{{- if $localNode.tolerations }}{{ $tolerations = $localNode.tolerations }}{{ end -}}
{{- if $tolerations }}
tolerations:
{{- toYaml $tolerations | nindent 2 }}
{{- end }}

{{- /* Handle nodeName */ -}}
{{- $nodeName := $globalNode.nodeName -}}
{{- if $localNode.nodeName }}{{ $nodeName = $localNode.nodeName }}{{ end -}}
{{- if $nodeName }}
nodeName: {{ $nodeName | quote }}
{{- end }}

{{- end -}}
