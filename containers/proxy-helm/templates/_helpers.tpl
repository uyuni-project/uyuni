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
