import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            //配置默认路由,用<router-view>就能够接收到
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomePage.vue'),
            children: [
                {
                    path: '/',
                    name: 'welcome-login',
                    component: () => import('@/components/welcome/LoginPage.vue')
                },
                {
                    path: 'register',
                    name: 'welcome-register',
                    component: () => import('@/components/welcome/RegisterPage.vue')
                }
            ]
        }, {
            //主页路由
            path: '/index',
            name: 'index',
            component: () => import('@/views/IndexPage.vue')

        }
    ]
})

export default router
