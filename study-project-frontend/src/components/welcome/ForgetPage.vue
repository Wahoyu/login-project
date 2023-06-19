<template>
  <div>
  <!--步骤条-->
  <div style="margin: 30px 20px">
    <el-steps :active="active" finish-status="success" align-center>
      <el-step title="验证邮箱"/>
      <el-step title="重置密码"/>
    </el-steps>
  </div>

  <!--邮箱验证-->
  <transition name="el-fade-in-linear" mode="out-in">
    <div style="text-align: center;margin: 0 20px;height: 100%" v-if="active === 0">

      <!--重置密码题头-->
      <div style="margin-top: 80px">
        <div style="font-size: 25px;font-weight: bold">邮箱验证</div>
        <div style="font-size: 14px;color: grey">请输入重置密码的电子邮件地址</div>
      </div>

      <!--表单-->
      <div style="margin-top: 50px">
        <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">

          <el-form-item prop="email">
            <el-input v-model="form.email" type="email" placeholder="电子邮件地址">
              <template #prefix>
                <el-icon>
                  <Message/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="code">
            <el-row :gutter="10" style="width: 100%">
              <el-col :span="17">
                <el-input v-model="form.code" :maxlength="6" type="text" placeholder="请输入验证码">
                  <template #prefix>
                    <el-icon>
                      <EditPen/>
                    </el-icon>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="5">
                <el-button type="success" @click="validateEmail"
                           :disabled="!isEmailValid || coldTime > 0">
                  {{ coldTime > 0 ? '获取验证码(' + coldTime + '秒)' : "获取验证码" }}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>
      </div>

      <!--验证邮箱button-->
      <div style="margin-top: 70px">
        <el-button @click="startReset()" style="width: 270px;" type="danger" plain>验证邮箱</el-button>
      </div>

      <!--跳转回登录界面-->
      <div style="margin-top: 20px">
        <span style="font-size: 14px;line-height: 15px;color: grey">想起账号?</span>
        <el-link type="primary" style="translate: 0 -2px" @click="router.push('/')">立即登录</el-link>
      </div>
    </div>
  </transition>

  <!--重置密码-->
  <transition name="el-fade-in-linear" mode="out-in">
    <div style="text-align: center;margin: 0 20px;height: 100%" v-if="active === 1">
      <!--重置密码题头-->
      <div style="margin-top: 80px">
        <div style="font-size: 25px;font-weight: bold">重置密码</div>
        <div style="font-size: 14px;color: grey">请填写您的新密码</div>
      </div>

      <!--新密码表单-->
      <div style="margin-top: 50px">
        <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">

          <el-form-item prop="password">
            <el-input v-model="form.password" :maxlength="16" type="password" placeholder="新密码">
              <template #prefix>
                <el-icon>
                  <Lock/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password_repeat">
            <el-input v-model="form.password_repeat" :maxlength="16" type="password" placeholder="重复新密码">
              <template #prefix>
                <el-icon>
                  <Lock/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>

      <!--重置密码按钮-->
      <div style="margin-top: 70px">
        <el-button @click="doReset()" style="width: 270px;" type="danger" plain>立即重置密码</el-button>
      </div>

      <!--返回登录界面-->
      <div style="margin-top: 20px">
        <span style="font-size: 14px;line-height: 15px;color: grey">想起账号?</span>
        <el-link type="primary" style="translate: 0 -2px" @click="router.push('/')">立即登录</el-link>
      </div>
    </div>
  </transition>



  </div>
</template>

<script setup>
import {reactive} from "vue";
import {EditPen, Message, Lock} from "@element-plus/icons-vue";
import {ref} from 'vue'
import router from "@/router";
import {post} from "@/net";
import {ElMessage} from "element-plus";

const active = ref(0)
const formRef = ref()
const form = reactive({
  email: '',
  code: '',
  password: '',
  password_repeat: ''
})


//邮箱验证码冷却时间
const coldTime = ref(0)

//发送验证码按钮显示设置
const isEmailValid = ref(false)
const onValidate = (prop, isValid) => {
  if (prop === 'email')
    isEmailValid.value = isValid
}

//重复输入密码校验逻辑
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

//参数校验规则
const rules = {
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 6, max: 16, message: '密码的长度必须在6-16字符之间', trigger: ['blur', 'change']}
  ],
  password_repeat: [
    {validator: validatePassword, trigger: ['blur', 'change']}
  ],
  email: [
    {required: true, message: '请输入邮件地址', trigger: 'blur'},
    {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
  ],
  code: [
    {required: true, message: '请输入获取的验证码', trigger: 'blur'},
  ]
}

//请求后端的发送验证码请求
const validateEmail = () => {
  post('/api/auth/valid-reset-email', {
    email: form.email
  }, (message) => {
    ElMessage.success(message)
    coldTime.value = 60
    setInterval(() => coldTime.value--, 1000)
  })
}

//
const startReset = () => {
  formRef.value.validate((isValid) => {
    if (isValid) {
      post('/api/auth/start-reset', {
        email: form.email,
        code: form.code
      }, () => {
        active.value++;
      })
    } else {
      ElMessage.warning('请填写电子邮件地址')
    }
  })
}

//
const doReset = () => {
  formRef.value.validate((isValid) => {
    if(isValid){
      post('/api/auth/do-reset',{
        password: form.password
      },(message)=>{
        ElMessage.success(message)
        router.push('/')
      })
    }else{
      ElMessage.warning('请填写新密码')
    }
  })
}
</script>

<style scoped>

</style>