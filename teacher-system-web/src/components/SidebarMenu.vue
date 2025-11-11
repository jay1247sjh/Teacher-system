<template>
    <aside class="sidebar-menu" :class="{ 'sidebar-collapsed': isCollapsed }">
        <div class="menu-wrapper">
            <div class="menu-toggle" @click="toggleCollapse">
                <span class="toggle-icon" :class="{ 'collapsed': isCollapsed }">☰</span>
            </div>

            <nav class="menu-nav">
                <ul class="menu-list">
                    <li
                        v-for="menuItem in menuItems"
                        :key="menuItem.id"
                        class="menu-item"
                        :class="{ 'active': activeMenuId === menuItem.id, 'has-children': menuItem.children?.length }"
                    >
                        <div
                            class="menu-title"
                            @click="handleMenuClick(menuItem)"
                        >
                            <span class="menu-icon">{{ menuItem.icon }}</span>
                            <span class="menu-text" v-show="!isCollapsed">{{ menuItem.title }}</span>
                            <span
                                v-if="menuItem.children?.length && !isCollapsed"
                                class="menu-arrow"
                                :class="{ 'expanded': expandedMenus.includes(menuItem.id) }"
                            >
                                ▼
                            </span>
                        </div>

                        <transition name="slide-down">
                            <ul
                                v-if="menuItem.children?.length && expandedMenus.includes(menuItem.id) && !isCollapsed"
                                class="submenu-list"
                            >
                                <li
                                    v-for="subItem in menuItem.children"
                                    :key="subItem.id"
                                    class="submenu-item"
                                    :class="{ 'active': activeSubMenuId === subItem.id }"
                                    @click="handleSubMenuClick(subItem)"
                                >
                                    <span class="submenu-icon">{{ subItem.icon }}</span>
                                    <span class="submenu-text">{{ subItem.title }}</span>
                                </li>
                            </ul>
                        </transition>
                    </li>
                </ul>
            </nav>
        </div>
    </aside>
</template>

<script lang="ts">
import {defineComponent, type PropType, ref} from 'vue'

interface MenuChild {
    id: string
    title: string
    icon: string
    path?: string
}

interface MenuItem {
    id: string
    title: string
    icon: string
    path?: string
    children?: MenuChild[]
}

export default defineComponent({
    name: 'SidebarMenu',
    props: {
        menuItems: {
            type: Array as PropType<MenuItem[]>,
            required: true
        },
        activeMenuId: {
            type: String,
            default: ''
        },
        activeSubMenuId: {
            type: String,
            default: ''
        }
    },
    emits: ['menu-click', 'submenu-click'],
    setup(_props, { emit }) {
        const isCollapsed = ref<boolean>(false)
        const expandedMenus = ref<string[]>([])

        const toggleCollapse = (): void => {
            isCollapsed.value = !isCollapsed.value
            if (isCollapsed.value) {
                expandedMenus.value = []
            }
        }

        const handleMenuClick = (menuItem: MenuItem): void => {
            if (menuItem.children?.length) {
                // 切换展开/折叠
                const index = expandedMenus.value.indexOf(menuItem.id)
                if (index > -1) {
                    expandedMenus.value.splice(index, 1)
                } else {
                    expandedMenus.value.push(menuItem.id)
                }
            }
            
            if (menuItem.path) {
                emit('menu-click', menuItem)
            }
        }

        const handleSubMenuClick = (subItem: MenuChild): void => {
            emit('submenu-click', subItem)
        }

        return {
            isCollapsed,
            expandedMenus,
            toggleCollapse,
            handleMenuClick,
            handleSubMenuClick
        }
    }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.sidebar-menu {
    width: 250px;
    min-height: 100vh;
    background: rgba($background-primary, 0.95);
    @include shadow(2);
    transition: width $transition-normal;
    border-right: 1px solid $border-color;
    position: relative;

    @include mobile {
        position: fixed;
        left: 0;
        top: 0;
        z-index: $z-index-fixed;
        transform: translateX(0);
        transition: transform $transition-normal;

        &.sidebar-collapsed {
            transform: translateX(-100%);
        }
    }
}

.sidebar-collapsed {
    width: 60px;

    .menu-text,
    .submenu-text {
        display: none;
    }
}

.menu-wrapper {
    @include flex-column;
    height: 100%;
}

.menu-toggle {
    height: 60px;
    @include flex-center;
    cursor: pointer;
    border-bottom: 1px solid $border-color;
    transition: background $transition-normal;

    &:hover {
        background: $background-secondary;
    }

    .toggle-icon {
        font-size: $font-size-xl;
        color: $text-primary;
        transition: transform $transition-normal;

        &.collapsed {
            transform: rotate(90deg);
        }
    }
}

.menu-nav {
    flex: 1;
    overflow-y: auto;
    @include custom-scrollbar;
    padding: $spacing-lg 0;
}

.menu-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.menu-item {
    margin-bottom: $spacing-xs;

    &.has-children {
        .menu-title {
            cursor: pointer;
        }
    }
}

.menu-title {
    @include flex-between;
    align-items: center;
    padding: $spacing-lg $spacing-xl;
    color: $text-primary;
    font-size: $font-size-md;
    font-weight: $font-weight-medium;
    transition: all $transition-normal;
    cursor: pointer;
    position: relative;

    &:hover {
        color: $primary-color;
        background: rgba($primary-color, 0.05);
    }

    .menu-icon {
        font-size: $font-size-lg;
        margin-right: $spacing-md;
        min-width: 20px;
        @include flex-center;
    }

    .menu-text {
        flex: 1;
        transition: opacity $transition-normal;
    }

    .menu-arrow {
        font-size: $font-size-sm;
        transition: transform $transition-normal;
        color: $text-muted;

        &.expanded {
            transform: rotate(180deg);
        }
    }
}

.menu-item.active {
    .menu-title {
        color: $primary-color;
        background: rgba($primary-color, 0.1);
        border-left: 3px solid $primary-color;
    }
}

.submenu-list {
    list-style: none;
    padding: 0;
    margin: 0;
    background: $background-secondary;
    overflow: hidden;
}

.submenu-item {
    padding: $spacing-md $spacing-xl $spacing-md $spacing-xxxl;
    color: $text-secondary;
    font-size: $font-size-sm;
    cursor: pointer;
    transition: all $transition-normal;
    @include flex-center;
    align-items: center;

    &:hover {
        color: $primary-color;
        background: rgba($primary-color, 0.05);
        padding-left: calc(#{$spacing-xxxl} + 5px);
    }

    .submenu-icon {
        font-size: $font-size-md;
        margin-right: $spacing-sm;
        min-width: 16px;
        @include flex-center;
    }

    .submenu-text {
        flex: 1;
    }
}

.submenu-item.active {
    color: $primary-color;
    background: rgba($primary-color, 0.1);
    border-left: 3px solid $primary-color;
    font-weight: $font-weight-medium;
}

// 动画效果
.slide-down-enter-active,
.slide-down-leave-active {
    transition: all $transition-normal;
    max-height: 500px;
    overflow: hidden;
}

.slide-down-enter-from,
.slide-down-leave-to {
    max-height: 0;
    opacity: 0;
}

// 响应式设计
@include mobile {
    .sidebar-menu {
        width: 250px;
    }

    .sidebar-collapsed {
        transform: translateX(-100%);
    }
}

@include tablet {
    .sidebar-menu {
        width: 200px;
    }

    .sidebar-collapsed {
        width: 60px;
    }
}
</style>
