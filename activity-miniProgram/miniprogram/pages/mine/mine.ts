const app = getApp<IAppOption>()
import getUserInfo from '../../utils/common'
import manage from '../../utils/manage'

Page({
    data: {
        userInfo: {} as any,
        hasUserInfo: false,
        canIUseGetUserProfile: false,

        img1: 'https://tdesign.gtimg.com/miniprogram/images/example1.png',
        pwd: "",
        visible: false
    },
    onChooseAvatar(e: any) {
      // 从事件对象中获取用户选择的头像URL
      const { avatarUrl } = e.detail;
      console.log(e);
      // 更新页面数据和全局数据
      const updatedUserInfo = {
          ...this.data.userInfo,  // 保留原有用户信息
          avatarUrl: avatarUrl    // 更新头像URL
      };
      
      // 设置页面数据
      this.setData({
          userInfo: updatedUserInfo
      });
      
      // 同时更新全局用户信息（如果需要）
      app.globalData.userInfo = updatedUserInfo;
      
      // 显示成功提示
      wx.showToast({
          title: '头像更新成功',
          icon: 'success'
      });
    },
    onLoad() {
        // @ts-ignore
        if (wx.getUserProfile) {
            this.setData({
                canIUseGetUserProfile: true
            })
            if (app.globalData.userInfo) {
                this.setData({
                    userInfo: app.globalData.userInfo,
                    hasUserInfo: true
                })
            }
        }
    },
    toManage() {
        manage.login({ pwd: this.data.pwd }).then((res) => {
            if (res.code == 200) {
                this.setData({
                    pwd: "",
                    visible: false
                })
                wx.navigateTo({
                    url: "../manage/manage"
                })
            } else {
                wx.showToast({
                    title: "密码错误",
                    icon: "error"
                })
            }
        })

    },

    getUserProfile() {
        getUserInfo().then((res) => {
            this.setData({
                userInfo: res,
                hasUserInfo: true
            })
        })
    },

    handlePopup(e: any) {
        const { item } = e.currentTarget.dataset;
        this.setData(
            {
                cur: item,
            },
            () => {
                this.setData({ visible: true });
            },
        );
    },
    onVisibleChange(e: any) {
        this.setData({
            visible: e.detail.visible,
        });
    },
    onClose() {
        this.setData({
            visible: false,
        });
    },
    onChange(e: any) {
        this.setData({
            pwd: e.detail.value
        })
    }
})
