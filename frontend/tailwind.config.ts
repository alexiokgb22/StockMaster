import type { Config } from 'tailwindcss'
import forms from '@tailwindcss/forms'

export default {
  content: ['./index.html', './src/**/*.{vue,ts}'],
  theme: {
    extend: {
      colors: {
        primary: '#0B3563',
        'primary-light': 'rgba(11,53,99,0.08)',
        accent: '#F8D830',
        'accent-secondary': '#F8B830',
        background: '#f0f4f8',
        surface: '#ffffff',
        border: '#d1dce8',
        'text-main': '#0B3563',
        'text-secondary': '#1e3a5f',
        error: '#ef4444',
      },
      boxShadow: {
        card: '0 24px 72px rgba(11, 53, 99, 0.08)',
      },
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [forms],
} satisfies Config
