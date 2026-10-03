/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}", "./projects/**/*.{html,ts}"],
  theme: {
    extend: {
      colors: {
        brand: { DEFAULT: "#1E3A8A", light: "#3B82F6", dark: "#172554" },
        accent: { DEFAULT: "#059669", light: "#34D399", dark: "#065F46" },
        warn: { DEFAULT: "#D97706" },
        danger: { DEFAULT: "#E11D48" },
      },
    },
  },
  plugins: [],
};
