/**
 * Online Exam System - Settings Panel
 */
(function() {
    'use strict';

    /**
     * Toggle the settings panel open/close
     */
    window.toggleSettings = function() {
        var panel = document.getElementById('settingsPanel');
        var overlay = document.getElementById('settingsOverlay');

        if (!panel || !overlay) return;

        var isOpen = panel.classList.contains('show');

        if (isOpen) {
            panel.classList.remove('show');
            overlay.classList.remove('show');
            document.body.style.overflow = '';
        } else {
            // Sync radio buttons with current theme
            var currentTheme = window.getTheme ? window.getTheme() : localStorage.getItem('exam-theme') || 'auto';
            var radio = document.getElementById('theme' + currentTheme.charAt(0).toUpperCase() + currentTheme.slice(1));
            if (radio) radio.checked = true;

            panel.classList.add('show');
            overlay.classList.add('show');
            document.body.style.overflow = 'hidden';
        }
    };

    /**
     * Close settings panel on Escape key
     */
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            var panel = document.getElementById('settingsPanel');
            if (panel && panel.classList.contains('show')) {
                window.toggleSettings();
            }
        }
    });

    /**
     * Close settings when clicking overlay
     */
    document.addEventListener('DOMContentLoaded', function() {
        var overlay = document.getElementById('settingsOverlay');
        if (overlay) {
            overlay.addEventListener('click', function(e) {
                if (e.target === overlay) {
                    window.toggleSettings();
                }
            });
        }
    });

    console.log('[Settings Panel] Initialized');
})();
