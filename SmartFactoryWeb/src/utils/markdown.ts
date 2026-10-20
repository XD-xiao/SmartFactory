import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

// 创建 markdown-it 实例，禁用原始 HTML 标签以防止 XSS 攻击
const md: MarkdownIt = new MarkdownIt({
  html: false,        // 禁用 HTML 标签，保障安全
  linkify: true,       // 自动将 URL 转换为链接
  breaks: true,        // 将换行符转换为 <br>
  typographer: false,   // 禁用排版替换，避免中文标点异常
  highlight: (str: string, lang: string): string => {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return (
          '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
        )
      } catch {
        // 高亮失败时降级为普通代码块
      }
    }
    // 自动检测语言或使用纯文本
    try {
      const result = hljs.highlightAuto(str)
      if (result.language) {
        return (
          '<pre class="hljs"><code>' +
          result.value +
          '</code></pre>'
        )
      }
    } catch {
      // 忽略
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

/**
 * 将 markdown 字符串解析为安全的 HTML 字符串
 * @param text - 原始 markdown 文本
 * @returns 解析后的 HTML 字符串，空值返回空字符串
 */
export function renderMarkdown(text: string | null | undefined): string {
  if (!text) {
    return ''
  }
  return md.render(text)
}

export default md
