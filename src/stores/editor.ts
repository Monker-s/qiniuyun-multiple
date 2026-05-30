import { ref, reactive } from 'vue'
import { defineStore } from 'pinia'
import type { Content, PlatformVersion } from '@/types/content'

export const useEditorStore = defineStore('editor', () => {
  const currentContent = ref<Content | null>(null)
  const platformVersions = reactive<Map<string, PlatformVersion>>(new Map())
  const adaptedHtmlMap = reactive<Map<string, string>>(new Map())
  const selectedPlatforms = ref<string[]>([])
  const activePreviewPlatform = ref<string>('WECHAT')
  const isSaving = ref(false)

  function setContent(content: Content) {
    currentContent.value = content
  }

  function setPlatformVersion(platformCode: string, version: PlatformVersion) {
    platformVersions.set(platformCode, version)
  }

  function getPlatformVersion(platformCode: string): PlatformVersion | undefined {
    return platformVersions.get(platformCode)
  }

  function togglePlatform(platformCode: string) {
    const idx = selectedPlatforms.value.indexOf(platformCode)
    if (idx >= 0) {
      selectedPlatforms.value.splice(idx, 1)
    } else {
      selectedPlatforms.value.push(platformCode)
    }
  }

  function selectAllPlatforms(codes: string[]) {
    selectedPlatforms.value = [...codes]
  }

  function deselectAllPlatforms() {
    selectedPlatforms.value = []
  }

  function setAdaptedHtml(platformCode: string, html: string) {
    adaptedHtmlMap.set(platformCode, html)
  }

  function getAdaptedHtml(platformCode: string): string | undefined {
    return adaptedHtmlMap.get(platformCode)
  }

  function clearAdaptedHtml() {
    adaptedHtmlMap.clear()
  }

  function clearVersions() {
    platformVersions.clear()
  }

  return {
    currentContent, platformVersions, adaptedHtmlMap, selectedPlatforms,
    activePreviewPlatform, isSaving,
    setContent, setPlatformVersion, getPlatformVersion,
    setAdaptedHtml, getAdaptedHtml, clearAdaptedHtml,
    togglePlatform, selectAllPlatforms, deselectAllPlatforms,
    clearVersions
  }
})
