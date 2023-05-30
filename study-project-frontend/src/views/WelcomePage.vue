<template>
  <div style="width: 100vw; height:100vh;overflow: hidden;display: flex">
    <!--左侧背景图-->
    <div style="flex: 1">
      <el-image style="width: 100%;height: 100%;" fit="cover"
                src="https://img1.baidu.com/it/u=4097856652,4033702227&fm=253&fmt=auto&app=120&f=JPEG?w=1422&h=800"/>
    </div>
    <!--左侧文字-->
    <div class="welcome-title">
      <div style="font-size: 30px;font-weight: bold">欢迎来到我们的学习平台</div>
      <div style="margin-top: 10px">在这里你可以学习如何使用Java,如何搭建网站,并且与Java之父汤文龙密切交流</div>
      <div style="margin-top: 10px">在这里你可以唱、跳、打篮球,做任何你喜欢的事</div>
    </div>

    <!--右侧登录界面-->
    <div style="width: 350px;background-color: white;z-index: 1">
      <router-view v-slot="{ Component }">
        <transition name="el-fade-in-linear">
          <component :is="Component"/>
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.welcome-title {
  position: absolute;
  bottom: 30px;
  left: 30px;
  color: white;
  text-shadow: 0 0 10px black;
}
</style>