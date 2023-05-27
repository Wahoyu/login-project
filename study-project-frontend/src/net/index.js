import axios from "axios";
import {ElMessage} from "element-plus";

const defaultError = () => ElMessage.error('发生了一些错误,请联系管理员')
const defaultFailure = (message) => ElMessage.warning(message)

//封装post方法
function post(url, data, success, failure = defaultFailure, error = defaultError) {
    axios.post(url, data, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        //发起请求携带Cookie,通过读取JSESSIONID识别用户,后续JWT不需要这行代码
        withCredentials: true
    }).then(({data}) => {
        if(data.success)
            success(data.message,data.status)
        else
            failure(data.message,data.status)
    }).catch(error)
}

//封装get方法
function get(url, success, failure = defaultFailure, error = defaultError) {
    axios.get(url, {
        withCredentials: true
    }).then(({data}) => {
        if(data.success)
            success(data.message,data.status)
        else
            failure(data.message,data.status)
    }).catch(error)
}

//对外暴露方法
export { get , post}