# Activity Transition Configuration

This document explains how to configure activity transitions in the Dividend Reminder App to exclude the AppBar from being part of the transition animation.

## Problem

By default, Android includes all UI elements (including the AppBar) in activity transitions, which can cause visual inconsistencies and make the AppBar appear to "jump" or be included in the transition animation.

## Solution

We've implemented multiple approaches to prevent the AppBar from being included in transitions:

### 1. Theme-based Approach (Recommended)

The app now uses custom themes with predefined transition configurations:

- **`Theme.MyDividendReminder.SlideTransition`**: Uses slide transitions with AppBar exclusion
- **`Theme.MyDividendReminder.NoAppBarTransition`**: Uses fade transitions with AppBar exclusion

These themes are applied in `AndroidManifest.xml`:

```xml
<activity
    android:name=".AddDividendActivity"
    android:theme="@style/Theme.MyDividendReminder.SlideTransition" />
```

### 2. Programmatic Approach (Fixed)

Use the `TransitionHelper` utility class for more control. **IMPORTANT**: The new setup methods must be called in `onCreate()` BEFORE `setContent()`:

```kotlin
class YourActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup transitions BEFORE setContent() to avoid requestFeature error
        TransitionHelper.setupCustomTransitions(this)
        
        setContent {
            // Your Compose content here
        }
    }
}
```

### 3. NavigationHelper Integration (Safe)

The `NavigationHelper` now uses the safe `overridePendingTransition` method:

```kotlin
val navigationHelper = NavigationHelper(this)
// All navigation methods now use safe fade transitions
navigationHelper.navigateToAddDividend()
```

## ⚠️ Important: Avoiding requestFeature Error

The `requestFeature()` method must be called BEFORE any content is added to the activity. To avoid the error:

1. **Call setup methods in onCreate() BEFORE setContent()**:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Setup transitions here
    TransitionHelper.setupCustomTransitions(this)
    
    // Then set content
    setContent { ... }
}
```

2. **Use the safe overridePendingTransition method** (already implemented in NavigationHelper)

3. **Use theme-based transitions** (most reliable approach)

## Transition Types Available

### Fade Transition (Recommended)
- **Pros**: Smooth, professional, AppBar exclusion works well
- **Cons**: Less visual feedback about direction
- **Use case**: General navigation, when you want subtle transitions

### Slide Transition
- **Pros**: Clear directional feedback
- **Cons**: May still include some AppBar elements if not configured properly
- **Use case**: When you want clear visual direction indication

### No Transition
- **Pros**: Instant switching, no animation artifacts
- **Cons**: No visual feedback, may feel abrupt
- **Use case**: When you want instant navigation, debugging

## Configuration Options

### In themes.xml
```xml
<style name="Theme.MyDividendReminder.CustomTransition" parent="Theme.MyDividendReminder">
    <item name="android:windowEnterTransition">@android:transition/fade</item>
    <item name="android:windowExitTransition">@android:transition/fade</item>
    <item name="android:windowAllowEnterTransitionOverlap">true</item>
    <item name="android:windowAllowReturnTransitionOverlap">true</item>
</style>
```

### In Activity onCreate() (Recommended)
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Choose one of these:
    TransitionHelper.setupCustomTransitions(this)      // Fade transitions
    TransitionHelper.setupSlideTransitions(this)       // Slide transitions
    
    setContent {
        // Your Compose content
    }
}
```

### Using NavigationHelper (Safest)
```kotlin
// Automatically uses safe transitions
val navigationHelper = NavigationHelper(this)
navigationHelper.navigateToAddDividend()
```

## Best Practices

1. **Use theme-based transitions** for the most reliable AppBar exclusion
2. **Call setup methods BEFORE setContent()** to avoid requestFeature errors
3. **Use NavigationHelper** for automatic safe transitions
4. **Test on different Android versions** as transition behavior can vary
5. **Keep transitions consistent** across the app for better UX

## Troubleshooting

### AppBar still appears in transition
- Ensure `allowEnterTransitionOverlap = true` is set
- Use fade transitions instead of slide transitions
- Check that the theme is properly applied in AndroidManifest.xml

### requestFeature() error
- **Most common cause**: Calling setup methods after setContent()
- **Solution**: Move transition setup to onCreate() BEFORE setContent()
- **Alternative**: Use NavigationHelper which handles this automatically

### Transitions not working on older devices
- Transitions require API level 21+ (Android 5.0)
- The TransitionHelper automatically handles version checking
- Fallback to standard navigation on unsupported devices

### Performance issues
- Use fade transitions for better performance
- Consider disabling transitions on low-end devices
- Monitor frame rates during transitions

## Migration Guide

If you're updating existing activities:

1. **Add transition setup in onCreate()**:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Add this line BEFORE setContent()
    TransitionHelper.setupCustomTransitions(this)
    
    setContent {
        // Existing content
    }
}
```

2. **Or use NavigationHelper** for automatic safe transitions

3. **Test thoroughly** to ensure no requestFeature errors

## Future Enhancements

- Add user preference setting for transition types
- Implement custom transition animations
- Add transition duration configuration
- Support for shared element transitions (excluding AppBar)
- Automatic transition setup through base activity classes
