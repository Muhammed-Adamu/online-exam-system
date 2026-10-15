/**
 * Online Exam System - Core Application JS
 * Sidebar toggle, toast notifications, modals, loading states, utilities
 */
(function() {
    'use strict';

    // Track ECharts instances for theme updates
    window.echartsInstances = window.echartsInstances || [];

    // Override echarts.init to track instances
    var originalEchartsInit = window.echarts.init;
    if (originalEchartsInit) {
        window.echarts.init = function() {
            var chart = originalEchartsInit.apply(this, arguments);
            window.echartsInstances.push(chart);
            return chart;
        };
    }

    // ============================================
    // Toast Notification System
    // ============================================
    window.showToast = function(message, type) {
        type = type || 'info';
        var icons = {
            success: 'bi-check-circle-fill',
            error: 'bi-exclamation-triangle-fill',
            warning: 'bi-exclamation-circle-fill',
            info: 'bi-info-circle-fill'
        };
        var bgColors = {
            success: '#06d6a0',
            error: '#ef476f',
            warning: '#ffd166',
            info: '#4361ee'
        };
        var textColor = type === 'warning' ? '#1a1a2e' : '#fff';

        var toast = document.createElement('div');
        toast.className = 'app-toast';
        toast.style.cssText = [
            'position: fixed',
            'top: 80px',
            'right: 20px',
            'padding: 14px 24px',
            'border-radius: 12px',
            'color: ' + textColor,
            'font-weight: 500',
            'z-index: 9999',
            'animation: slideInRight 0.35s cubic-bezier(0.4, 0, 0.2, 1)',
            'background: ' + (bgColors[type] || bgColors.info),
            'box-shadow: 0 8px 24px rgba(0,0,0,0.2)',
            'max-width: 400px',
            'display: flex',
            'align-items: center',
            'gap: 10px',
            'font-size: 0.95rem',
            'pointer-events: auto',
            'backdrop-filter: blur(8px)'
        ].join(';');

        toast.innerHTML = '<i class="bi ' + (icons[type] || icons.info) + '" style="font-size:1.3rem;flex-shrink:0;"></i>' +
                          '<span>' + message + '</span>';
        document.body.appendChild(toast);

        // Click to dismiss
        toast.addEventListener('click', function() { dismissToast(toast); });

        // Auto dismiss
        var timeout = type === 'error' ? 8000 : (type === 'warning' ? 6000 : 4000);
        setTimeout(function() { dismissToast(toast); }, timeout);
    };

    function dismissToast(toast) {
        if (!toast || toast._dismissing) return;
        toast._dismissing = true;
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
        setTimeout(function() {
            if (toast.parentNode) toast.parentNode.removeChild(toast);
        }, 300);
    }

    // ============================================
    // Confirmation Modal
    // ============================================
    window.showConfirmModal = function(message, onConfirm, options) {
        options = options || {};
        var modal = document.getElementById('confirmModal');
        if (!modal) {
            // Fallback to browser confirm if modal doesn't exist
            if (confirm(message) && typeof onConfirm === 'function') {
                onConfirm();
            }
            return;
        }

        var messageEl = document.getElementById('confirmModalMessage');
        var titleEl = document.getElementById('confirmModalTitle');
        var confirmBtn = document.getElementById('confirmModalBtn');
        var cancelBtn = document.getElementById('confirmModalCancel');

        if (titleEl) titleEl.textContent = options.title || 'Confirm Action';
        if (messageEl) messageEl.innerHTML = message;
        if (confirmBtn) {
            confirmBtn.textContent = options.confirmText || 'Confirm';
            confirmBtn.className = 'btn ' + (options.confirmClass || 'btn-danger');
            // Remove old listener by cloning
            var newBtn = confirmBtn.cloneNode(true);
            confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);
            newBtn.addEventListener('click', function() {
                bootstrap.Modal.getInstance(modal).hide();
                if (typeof onConfirm === 'function') onConfirm();
            });
        }
        if (cancelBtn) {
            cancelBtn.textContent = options.cancelText || 'Cancel';
        }

        var bsModal = new bootstrap.Modal(modal, { backdrop: 'static' });
        bsModal.show();
    };

    // For link-based confirms (navigation)
    window.confirmLink = function(event, message, options) {
        event.preventDefault();
        var link = event.currentTarget;
        var href = link.getAttribute('href');
        showConfirmModal(message, function() {
            window.location.href = href;
        }, options);
    };

    // ============================================
    // Loading States
    // ============================================
    window.showLoading = function(btnOrSelector) {
        var btn = typeof btnOrSelector === 'string' ? document.querySelector(btnOrSelector) : btnOrSelector;
        if (!btn) return;
        btn._originalHtml = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>' +
                        (btn.getAttribute('data-loading-text') || 'Processing...');
        btn.classList.add('btn-loading');
    };

    window.hideLoading = function(btnOrSelector) {
        var btn = typeof btnOrSelector === 'string' ? document.querySelector(btnOrSelector) : btnOrSelector;
        if (!btn) return;
        btn.disabled = false;
        if (btn._originalHtml) {
            btn.innerHTML = btn._originalHtml;
        }
        btn.classList.remove('btn-loading');
    };

    // Auto-add loading state to all form submit buttons
    document.addEventListener('submit', function(e) {
        var form = e.target;
        if (form.classList.contains('no-auto-loading')) return;
        var submitBtn = form.querySelector('button[type="submit"]');
        if (submitBtn) {
            showLoading(submitBtn);
        }
    }, true);

    // ============================================
    // Sidebar Toggle
    // ============================================
    window.toggleSidebar = function() {
        var sidebar = document.getElementById('sidebar');
        if (sidebar) {
            sidebar.classList.toggle('show');
        }
    };

    // ============================================
    // Session Timeout Monitor
    // ============================================
    var idleTimeout = 28 * 60 * 1000; // 28 minutes
    var warningTimeout = 25 * 60 * 1000; // 25 minutes
    var idleTimer, warningTimer;
    var warningShown = false;

    function resetIdleTimer() {
        clearTimeout(idleTimer);
        clearTimeout(warningTimer);
        if (warningShown) {
            warningShown = false;
            var warningModal = document.getElementById('sessionWarningModal');
            if (warningModal) {
                try { bootstrap.Modal.getInstance(warningModal).hide(); } catch(e) {}
            }
        }
        warningTimer = setTimeout(showSessionWarning, warningTimeout);
        idleTimer = setTimeout(sessionExpired, idleTimeout);
    }

    function showSessionWarning() {
        warningShown = true;
        var modal = document.getElementById('sessionWarningModal');
        if (modal) {
            try {
                var bsModal = new bootstrap.Modal(modal, { backdrop: 'static', keyboard: false });
                bsModal.show();
            } catch(e) {}
        }
    }

    function sessionExpired() {
        window.location.href = '/exam/login?error=session_expired';
    }

    window.resetSessionTimer = resetIdleTimer;

    // Track user activity
    var activityEvents = ['mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart', 'click'];
    activityEvents.forEach(function(evt) {
        document.addEventListener(evt, resetIdleTimer, { passive: true });
    });

    resetIdleTimer();

    // ============================================
    // Utility Functions
    // ============================================
    window.formatDate = function(dateStr) {
        if (!dateStr) return '-';
        var d = new Date(dateStr);
        return d.toLocaleDateString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    };

    window.ajaxPost = function(url, data, options) {
        options = options || {};
        return fetch(url, {
            method: 'POST',
            headers: options.headers || { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: data instanceof URLSearchParams ? data.toString() :
                  (typeof data === 'string' ? data : new URLSearchParams(data).toString())
        })
        .then(function(response) {
            if (!response.ok) throw new Error('Network error: ' + response.status);
            var ct = response.headers.get('content-type') || '';
            return ct.includes('application/json') ? response.json() : response.text();
        });
    };

    // ============================================
    // Initialize on DOM Ready
    // ============================================
    document.addEventListener('DOMContentLoaded', function() {
        // Auto-dismiss server-rendered alerts after 5 seconds (keep them but fade)
        setTimeout(function() {
            var alerts = document.querySelectorAll('.alert-dismissible');
            alerts.forEach(function(alert) {
                try { new bootstrap.Alert(alert).close(); } catch(e) {}
            });
        }, 5000);

        // Handle confirm-link elements with data-confirm attribute
        document.querySelectorAll('.confirm-link').forEach(function(link) {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                var message = this.getAttribute('data-confirm') || 'Are you sure?';
                var title = this.getAttribute('data-confirm-title') || 'Confirm Action';
                var confirmClass = this.getAttribute('data-confirm-class') || 'btn-danger';
                var href = this.getAttribute('href');
                showConfirmModal(message, function() {
                    window.location.href = href;
                }, { title: title, confirmClass: confirmClass });
            });
        });

        // Close sidebar on mobile when clicking main content
        var mainContent = document.getElementById('mainContent');
        if (mainContent) {
            mainContent.addEventListener('click', function() {
                var sidebar = document.getElementById('sidebar');
                if (sidebar && sidebar.classList.contains('show') && window.innerWidth <= 768) {
                    sidebar.classList.remove('show');
                }
            });
        }

        // Display any server flash messages as toasts too
        var msgEl = document.querySelector('.alert-success');
        if (msgEl && msgEl.textContent.trim()) {
            window.showToast(msgEl.textContent.trim(), 'success');
        }
        var errEl = document.querySelector('.alert-danger');
        if (errEl && errEl.textContent.trim()) {
            window.showToast(errEl.textContent.trim(), 'error');
        }

        // Start notification polling
        var bell = document.getElementById('notificationBell');
        if (bell) {
            pollUnreadCount();
            notificationPollTimer = setInterval(pollUnreadCount, notificationPollInterval);
        }

        console.log('[App] Initialized - Online Exam System v1.0.0');
    });

    // ============================================
    // Notification System
    // ============================================
    var notificationPollTimer = null;
    var notificationPollInterval = 30000; // 30 seconds
    var lastUnreadCount = 0;

    window.toggleNotificationDropdown = function() {
        var dropdown = document.getElementById('notificationDropdown');
        if (!dropdown) return;
        var isOpen = dropdown.classList.contains('show');

        if (!isOpen) {
            loadNotifications();
            dropdown.classList.add('show');
            // Close settings panel if open
            var settingsPanel = document.getElementById('settingsPanel');
            var settingsOverlay = document.getElementById('settingsOverlay');
            if (settingsPanel && settingsPanel.classList.contains('show')) {
                settingsPanel.classList.remove('show');
                if (settingsOverlay) settingsOverlay.classList.remove('show');
            }
        } else {
            dropdown.classList.remove('show');
        }
    };

    function loadNotifications() {
        var list = document.getElementById('notificationList');
        var footerText = document.getElementById('notificationFooterText');
        if (!list) return;

        fetch(ctxPath + '/api/notifications?limit=20')
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (!data.success) return;

                var notifications = data.notifications || [];
                var unreadCount = data.unreadCount || 0;

                if (footerText) {
                    footerText.textContent = unreadCount + ' unread';
                }

                if (notifications.length === 0) {
                    list.innerHTML = '<div class="notification-empty">' +
                        '<i class="bi bi-bell-slash"></i>' +
                        '<p class="mb-0">No notifications yet</p>' +
                        '<small>New alerts will appear here</small>' +
                        '</div>';
                } else {
                    list.innerHTML = notifications.map(function(n) {
                        var type = n.type || '';
                        var refId = n.referenceId || 0;
                        var paperId = n.paperId || 0;
                        var iconClass = (type === 'EXAM_SUBMITTED') ? 'bi bi-file-earmark-check' :
                                        (type === 'EXAM_GRADED') ? 'bi bi-check-circle' :
                                        (type === 'TEACHER_NOTICE') ? 'bi bi-chat-dots' : 'bi bi-bell';
                        return '<a class="notification-item' + (n.isRead === 0 ? ' unread' : '') + '"' +
                            ' href="javascript:void(0)"' +
                            ' onclick="markNotificationRead(' + n.id + ', \'' + type + '\', ' + refId + ', ' + paperId + ')"' +
                            ' title="Click to view">' +
                            '<div class="notif-icon">' +
                                '<i class="' + iconClass + '"></i>' +
                            '</div>' +
                            '<div class="notif-content">' +
                                '<div class="notif-message">' + escapeHtml(n.message) + '</div>' +
                                '<div class="notif-meta">' +
                                    '<span class="notif-time">' + escapeHtml(n.timeAgo || '') + '</span>' +
                                    '<span class="notif-link-hint">View &rarr;</span>' +
                                '</div>' +
                            '</div>' +
                        '</a>';
                    }).join('');
                }

                updateBadge(unreadCount);
                lastUnreadCount = unreadCount;
            })
            .catch(function(err) {
                console.error('[Notifications] Failed to load:', err);
            });
    }

    function updateBadge(count) {
        var badge = document.getElementById('notificationBadge');
        if (!badge) return;

        if (count > 0) {
            badge.textContent = count > 99 ? '99+' : count;
            badge.classList.remove('d-none');
            // Animate if new notifications arrived
            if (count > lastUnreadCount) {
                var bell = document.getElementById('notificationBell');
                if (bell) {
                    bell.classList.remove('notification-bell-ring');
                    void bell.offsetWidth; // trigger reflow
                    bell.classList.add('notification-bell-ring');
                }
            }
        } else {
            badge.classList.add('d-none');
            badge.textContent = '0';
        }
    }

    function pollUnreadCount() {
        fetch(ctxPath + '/api/notifications/unread-count')
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (data.success) {
                    updateBadge(data.count || 0);
                    lastUnreadCount = data.count || 0;
                }
            })
            .catch(function(err) {
                // Silently ignore polling errors
            });
    }

    window.markNotificationRead = function(id, type, refId, paperId) {
        // Mark as read on server
        fetch(ctxPath + '/api/notifications/' + id + '/read', { method: 'POST' })
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (data.success) {
                    pollUnreadCount();
                }
            })
            .catch(function() {});

        // Close dropdown
        var dropdown = document.getElementById('notificationDropdown');
        if (dropdown) dropdown.classList.remove('show');

        // Route based on notification type
        if (type === 'EXAM_SUBMITTED') {
            // Teacher: go to score analysis for the paper
            if (paperId && paperId > 0) {
                window.location.href = ctxPath + '/teacher/analysis?paperId=' + paperId;
            } else {
                window.location.href = ctxPath + '/teacher/analysis';
            }
        } else if (type === 'EXAM_GRADED') {
            // Student: go to exam result page
            if (refId && refId > 0) {
                window.location.href = ctxPath + '/student/exam/result/' + refId;
            } else {
                window.location.href = ctxPath + '/student/scores';
            }
        } else if (type === 'TEACHER_NOTICE') {
            // Student: notice from teacher — go to scores page
            window.location.href = ctxPath + '/student/scores';
        } else {
            // Fallback: go to dashboard
            window.location.href = ctxPath + '/';
        }
    };

    window.markAllNotificationsRead = function() {
        fetch(ctxPath + '/api/notifications/read-all', { method: 'POST' })
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (data.success) {
                    pollUnreadCount();
                    // Update the list items to remove unread styling
                    var items = document.querySelectorAll('.notification-item.unread');
                    items.forEach(function(item) { item.classList.remove('unread'); });
                    var footerText = document.getElementById('notificationFooterText');
                    if (footerText) footerText.textContent = '0 unread';
                    window.showToast('All notifications marked as read', 'info');
                }
            })
            .catch(function(err) {
                console.error('[Notifications] Failed to mark all read:', err);
            });
    };

    function escapeHtml(text) {
        if (!text) return '';
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // Close notification dropdown on outside click
    document.addEventListener('click', function(e) {
        var wrapper = document.getElementById('notificationWrapper');
        var dropdown = document.getElementById('notificationDropdown');
        if (wrapper && dropdown && dropdown.classList.contains('show')) {
            if (!wrapper.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        }
    });

    // Inject animation keyframes
    var styleEl = document.createElement('style');
    styleEl.textContent = [
        '@keyframes slideInRight {',
        '  from { transform: translateX(120%); opacity: 0; }',
        '  to { transform: translateX(0); opacity: 1; }',
        '}',
        '.btn-loading {',
        '  pointer-events: none;',
        '  opacity: 0.75;',
        '}',
        '.app-toast {',
        '  transition: opacity 0.3s ease, transform 0.3s ease;',
        '}'
    ].join('\n');
    document.head.appendChild(styleEl);

    console.log('[App Core] Loaded');
})();
