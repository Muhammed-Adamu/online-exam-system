/**
 * Online Exam System - Theme Manager
 * Supports: Light, Dark, System (auto)
 */
(function() {
    'use strict';

    var STORAGE_KEY = 'exam-theme';
    var ATTR = 'data-theme';

    /**
     * Set the active theme
     * @param {string} theme - 'light', 'dark', or 'auto'
     */
    window.setTheme = function(theme) {
        if (!['light', 'dark', 'auto'].includes(theme)) {
            theme = 'auto';
        }

        // Apply theme to document
        document.documentElement.setAttribute(ATTR, theme);

        // Save preference
        localStorage.setItem(STORAGE_KEY, theme);

        // Update settings panel radio buttons
        var radio = document.getElementById('theme' + theme.charAt(0).toUpperCase() + theme.slice(1));
        if (radio) {
            radio.checked = true;
        }

        // Update ECharts instances if they exist
        updateChartThemes(theme);

        console.log('[Theme] Applied:', theme);
    };

    /**
     * Get the current active theme
     * @returns {string} 'light', 'dark', or 'auto'
     */
    window.getTheme = function() {
        var saved = localStorage.getItem(STORAGE_KEY);
        return saved || 'auto';
    };

    /**
     * Get the resolved theme (what's actually rendered)
     * @returns {string} 'light' or 'dark'
     */
    window.getResolvedTheme = function() {
        var theme = document.documentElement.getAttribute(ATTR);
        if (theme === 'auto') {
            return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
        }
        return theme || 'light';
    };

    /**
     * Update ECharts theme based on current color scheme
     */
    function updateChartThemes(theme) {
        var isDark = false;
        if (theme === 'dark') {
            isDark = true;
        } else if (theme === 'auto') {
            isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        }

        // Update all ECharts instances
        if (window.echartsInstances && window.echartsInstances.length > 0) {
            window.echartsInstances.forEach(function(chart) {
                try {
                    if (chart && !chart.isDisposed()) {
                        // Update text color for existing charts
                        var options = chart.getOption();
                        // Trigger a resize which forces re-render with current CSS vars
                        chart.resize();
                    }
                } catch(e) {}
            });
        }
    }

    /**
     * Initialize theme on page load
     */
    function initTheme() {
        var savedTheme = localStorage.getItem(STORAGE_KEY) || 'auto';
        document.documentElement.setAttribute(ATTR, savedTheme);

        // Set radio button in settings panel if present
        var radio = document.getElementById('theme' + savedTheme.charAt(0).toUpperCase() + savedTheme.slice(1));
        if (radio) {
            radio.checked = true;
        }

        // Listen for OS theme changes when in auto mode
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function(e) {
            if (localStorage.getItem(STORAGE_KEY) === 'auto' || !localStorage.getItem(STORAGE_KEY)) {
                document.documentElement.setAttribute(ATTR, 'auto');
                updateChartThemes('auto');
            }
        });
    }

    /**
     * Font size management
     */
    window.changeFontSize = function(size) {
        var root = document.documentElement;
        switch(size) {
            case 'small':
                root.style.fontSize = '14px';
                break;
            case 'large':
                root.style.fontSize = '18px';
                break;
            default:
                root.style.fontSize = '16px';
        }
        localStorage.setItem('exam-font-size', size);
    };

    // Initialize font size
    (function() {
        var savedSize = localStorage.getItem('exam-font-size') || 'normal';
        window.changeFontSize(savedSize);
        var select = document.getElementById('fontSizeSelect');
        if (select) {
            select.value = savedSize;
        }
    })();

    // Initialize on load
    initTheme();

    console.log('[Theme Manager] Initialized');
})();
