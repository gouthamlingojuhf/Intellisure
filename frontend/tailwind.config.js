/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}", "./projects/**/*.{html,ts}"],
  theme: {
    extend: {
      colors: {
        ink: "#000000",
        claret: { DEFAULT: "#75013F", hover: "#8F1750", soft: "#F8EAF1" },
        fuchsia: { DEFAULT: "#FE3082", soft: "#FFF0F6" },
        warm: { DEFAULT: "#EAE5DF", light: "#F7F5F3", dark: "#D8D0CA" },
        success: { DEFAULT: "#176B45", soft: "#EAF6F0" },
        warning: { DEFAULT: "#9A5B00", soft: "#FFF5DC" },
        danger: { DEFAULT: "#A32120", soft: "#FDECEA" },
      },
      boxShadow: {
        panel: "0 1px 2px rgba(0, 0, 0, 0.04), 0 8px 24px rgba(0, 0, 0, 0.05)",
        elevated: "0 18px 48px rgba(0, 0, 0, 0.12)",
      },
      fontFamily: {
        sans: ["Inter", "Segoe UI", "Helvetica Neue", "Arial", "sans-serif"],
      },
    },
  },
  plugins: [],
};
