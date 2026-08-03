---
name: VisionTwin AI
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#434655'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#737686'
  outline-variant: '#c3c6d7'
  surface-tint: '#0053db'
  primary: '#004ac6'
  on-primary: '#ffffff'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#b4c5ff'
  secondary: '#712ae2'
  on-secondary: '#ffffff'
  secondary-container: '#8a4cfc'
  on-secondary-container: '#fffbff'
  tertiary: '#005e6e'
  on-tertiary: '#ffffff'
  tertiary-container: '#00788c'
  on-tertiary-container: '#d7f6ff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#eaddff'
  secondary-fixed-dim: '#d2bbff'
  on-secondary-fixed: '#25005a'
  on-secondary-fixed-variant: '#5a00c6'
  tertiary-fixed: '#acedff'
  tertiary-fixed-dim: '#4cd7f6'
  on-tertiary-fixed: '#001f26'
  on-tertiary-fixed-variant: '#004e5c'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  display:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  headline-md:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.03em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  sidebar_width: 280px
  container_max_width: 1440px
  gutter: 24px
  margin_mobile: 16px
  margin_desktop: 32px
  stack_sm: 8px
  stack_md: 16px
  stack_lg: 24px
---

## Brand & Style

The design system is engineered for the high-stakes environment of industrial textile manufacturing, where precision meets high-velocity automation. The brand personality is **Intelligent, Reliable, and Observant**. It avoids the clutter of traditional industrial software in favor of a **Minimalist-Modern** aesthetic inspired by elite developer tools.

The visual narrative centers on "The Digital Twin"—a clean, hyper-accurate representation of physical assets. By utilizing generous whitespace and a "content-first" hierarchy, the UI reduces cognitive load for operators and managers, allowing AI-driven insights to stand out through purposeful accents. The emotional response should be one of absolute control and technological sophistication.

## Colors

The palette is anchored in a clinical, high-end "Tech-Light" mode. 

- **Primary Blue (#2563EB):** Reserved for functional SaaS actions, primary navigation states, and standard system interactions.
- **AI Accent (#7C3AED):** This color is used exclusively for intelligence-driven features, such as predictive maintenance alerts, diagnostic insights, and machine-learning visualizations.
- **Surface Hierarchy:** The background uses a cool slate-white to reduce glare, while cards use pure white to pop from the canvas, separated by hairline borders.
- **Typography Contrast:** We use a strict three-tier grayscale to ensure readability against industrial hardware interfaces.

## Typography

This design system utilizes **Inter** across all layers to maintain a systematic, utilitarian feel that scales perfectly from desktop dashboards to ruggedized tablet displays.

- **Weight Strategy:** Use *SemiBold (600)* for interactive elements and *Bold (700)* sparingly for page titles. *Medium (500)* is the workhorse for labels and secondary navigation.
- **Optical Sizing:** Display styles use negative letter spacing to feel tighter and more premium.
- **Hierarchy:** Ensure a clear distinction between data (Body-MD) and metadata (Label-SM). Labels should often be displayed in `text-secondary` or `text-muted` to emphasize the data values themselves.

## Layout & Spacing

The layout follows a **Fixed Sidebar + Fluid Content** model. The sidebar is a constant anchor for navigation, while the main content area utilizes a flexible grid that expands to a maximum width of 1440px to ensure line lengths remain readable on ultra-wide industrial monitors.

- **Sidebar:** 280px wide with a slight inner margin to house 16px border-radius navigation items. 
- **Grid:** A 12-column system with 24px gutters. 
- **Spacing Rhythm:** Based on an 8px base unit. Component internal padding should default to 16px (MD) or 24px (LG) to ensure high touch-target visibility for factory floor use.
- **Breakpoints:** 
  - Mobile: <768px (Sidebar becomes a bottom drawer or hidden hamburger).
  - Tablet: 768px - 1024px (Sidebar collapses to icon-only rail).
  - Desktop: >1024px (Full expanded sidebar).

## Elevation & Depth

This design system employs a **Flat-Tonal** approach with "Surface-on-Surface" depth rather than heavy shadows.

- **Tiers:**
  - **Level 0 (Background):** #F8FAFC. The foundation.
  - **Level 1 (Cards/Sidebar):** #FFFFFF. White surfaces with a 1px border (#E2E8F0).
  - **Level 2 (Dropdowns/Modals):** White surfaces with a soft, ultra-diffused shadow (0px 8px 24px rgba(15, 23, 42, 0.08)) and a #E2E8F0 border.
- **AI Depth:** Components with AI-specific data (Predictive Maintenance cards) utilize a subtle 1px internal top-border or glow using a 10% opacity version of the AI Accent color (#7C3AED).

## Shapes

The shape language is **Structured and Friendly**. 

- **Standard Elements:** Buttons, Inputs, and Cards use the `rounded-md` (0.5rem) setting.
- **Sidebar & Containers:** Large containers and the sidebar navigation items use `rounded-lg` (1rem) or `rounded-xl` (1.5rem) to create a soft, "encapsulated" feel that differentiates the software from the rigid, sharp lines of factory machinery.
- **Data Points:** Status indicators and small badges use "Pill" shapes (999px) to clearly distinguish them from interactive buttons.

## Components

- **Industrial Buttons:** Large touch targets (min-height: 44px). Primary buttons use #2563EB with white text. AI-specific buttons use a gradient-border or a subtle #7C3AED background.
- **KPI Cards:** Feature a large `headline-lg` value. A small trend sparkline should be positioned at the bottom, using Success (Green) or Danger (Red).
- **AI Diagnostic Cards:** Distinguished by a thin #7C3AED left-border. Use "Inter-Medium" for diagnostic text to ensure authority.
- **Data Tables:** No vertical lines. 1px horizontal borders only (#E2E8F0). Header row uses `label-sm` with `text-muted` and uppercase styling.
- **Sidebar Items:** Inspired by Linear; use a 16px horizontal padding, 8px vertical padding. Active state uses a light gray background (#F1F5F9) and a 2px vertical blue indicator on the far left.
- **Input Fields:** High-contrast borders (#E2E8F0) that darken to #2563EB on focus. Use a 14px (Body-MD) font size for input text to ensure clarity.