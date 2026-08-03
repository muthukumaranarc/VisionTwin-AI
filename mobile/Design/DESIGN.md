---
name: VisionTwin AI Mobile
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
  on-surface-variant: '#424656'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#727687'
  outline-variant: '#c2c6d8'
  surface-tint: '#0054d6'
  primary: '#0050cb'
  on-primary: '#ffffff'
  primary-container: '#0066ff'
  on-primary-container: '#f8f7ff'
  inverse-primary: '#b3c5ff'
  secondary: '#585f6c'
  on-secondary: '#ffffff'
  secondary-container: '#dce2f3'
  on-secondary-container: '#5e6572'
  tertiary: '#575a5b'
  on-tertiary: '#ffffff'
  tertiary-container: '#707274'
  on-tertiary-container: '#f7f8fa'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dae1ff'
  primary-fixed-dim: '#b3c5ff'
  on-primary-fixed: '#001849'
  on-primary-fixed-variant: '#003fa4'
  secondary-fixed: '#dce2f3'
  secondary-fixed-dim: '#c0c7d6'
  on-secondary-fixed: '#151c27'
  on-secondary-fixed-variant: '#404754'
  tertiary-fixed: '#e1e2e4'
  tertiary-fixed-dim: '#c5c6c8'
  on-tertiary-fixed: '#191c1e'
  on-tertiary-fixed-variant: '#444749'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  headline-lg:
    fontFamily: Hanken Grotesk
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 34px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Hanken Grotesk
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
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
  label-caps:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.05em
  button:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  touch-target-min: 44px
  margin-mobile: 16px
  gutter-mobile: 12px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 24px
---

## Brand & Style
The design system focuses on high-precision AI visualization adapted for handheld interaction. The brand personality is technical yet frictionless, evoking a sense of "intelligence in your pocket." 

The design style is **Corporate / Modern** with a lean towards **Minimalism**. It utilizes expansive white space, subtle silver-grey scales, and high-precision typography. The mobile adaptation prioritizes verticality and thumb-reachable zones while maintaining the sophisticated, data-driven aesthetic of enterprise-grade AI tools.

## Colors
The palette is rooted in a "Stripe-inspired" light aesthetic. 

- **Primary**: A vivid, high-contrast blue used sparingly for primary actions, progress indicators, and active states.
- **Neutral**: Deep slates and navy-blacks for typography to ensure maximum legibility under varying outdoor light conditions.
- **Surface**: Pure white (#FFFFFF) for the base layer, with subtle grey (#F9FAFB) for secondary containers to create clear structural separation without heavy borders.

## Typography
Typography is condensed to optimize for the limited horizontal real estate of mobile screens. 

- **Headlines**: Use Hanken Grotesk with tight letter-spacing to feel modern and authoritative.
- **Body**: Inter provides high legibility for dense AI data descriptions.
- **Data/Technical**: JetBrains Mono is used for IDs, timestamps, and AI confidence scores to reinforce the "Twin" digital-replica nature of the product.
- **Mobile Scaling**: Headlines are capped at 28px to prevent excessive wrapping.

## Layout & Spacing
This design system utilizes a **Fluid Grid** optimized for a 4-column mobile layout. 

- **Touch Safety**: Every interactive element (buttons, list items, toggles) must adhere to a minimum height of 44px. 
- **Margins**: A consistent 16px lateral margin ensures content does not feel "crowded" against device edges.
- **Vertical Rhythm**: Content is stacked using an 8px base unit. Actions and primary sections are separated by 24px (stack-lg) to allow for clear thumb-tap targeting.

## Elevation & Depth
Depth is conveyed through **Tonal Layers** and **Ambient Shadows** to mimic the refined feel of modern SaaS platforms.

- **Base Layer**: White background.
- **Content Cards**: Use a subtle 1px border (#E5E7EB) with a very soft, diffused shadow (0px 4px 12px rgba(0,0,0,0.05)).
- **Bottom Sheets**: These are the primary navigational and filtering pattern. They emerge from the bottom with a 20% black backdrop overlay, using a 16px corner radius on top edges to indicate "physical" layering over the current view.

## Shapes
The shape language is **Soft**, leaning toward professional precision rather than playfulness.

- **Standard Elements**: 4px (0.25rem) radius for inputs and small chips.
- **Containers/Cards**: 8px (0.5rem) radius to define clear sections of content.
- **Bottom Sheets & Modals**: 12px (0.75rem) radius on top corners to provide a distinct "docked" appearance.

## Components

- **Buttons**: Primary buttons are full-width (mobile-responsive) with a height of 48px. Use high-contrast backgrounds (Primary Blue) with white text.
- **Bottom Sheets**: Used for all filtering and AI configuration settings. They must include a "grab handle" indicator (32x4px, centered, grey-300).
- **Input Fields**: 48px height with inset labels. Borders should turn Primary Blue on focus.
- **Chips**: Used for status indicators (e.g., "AI Processing," "Synced"). These use the Label-Caps typography and light background tints of their status color.
- **Lists**: Interactive list items must have a minimum height of 56px to provide ample vertical hit area, separated by subtle hairline dividers (#F3F4F6).
- **Cards**: Summary cards for AI twins use 16px internal padding and light grey borders to group related data points without overwhelming the user with heavy shadows.