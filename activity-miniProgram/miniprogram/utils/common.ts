import api from './loginApi'
const app = getApp<IAppOption>()

export default function getUserProfile() {
    // 推荐使用wx.getUserProfile获取用户信息，开发者每次通过该接口获取用户个人信息均需用户确认，开发者妥善保管用户快速填写的头像昵称，避免重复弹窗
    wx.getSetting({
      success(res) {
        if (res.authSetting['scope.userInfo']) {
          // 已授权，可以获取真实昵称
          console.log("用户已授权，可以获取真实昵称");
        } else {
          // 未授权，需引导用户点击按钮授权
          console.log("用户未授权");
        }
      }
    });
    return new Promise((resolve, reject) => {
        wx.getUserProfile({
            desc: '展示用户信息',
            success: (res) => {
                api.login(res.userInfo)
                app.globalData.userInfo = res.userInfo
                wx.showToast({
                    title: '成功',
                    icon: 'success',
                    duration: 2000
                })
                resolve(res.userInfo)
            },
            fail: () => {
                wx.showToast({
                    title: '失败',
                    icon: 'error',
                    duration: 2000
                })
                reject()
            }
        })
    })
}