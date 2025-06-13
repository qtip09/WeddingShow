import api from "../../utils/activityApi"
import requestByOpenid from "../../utils/requestByOpenid"
import getUserInfo from '../../utils/common'
let app = getApp()
import baseUrl from '../../utils/base'
Page({

    /**
     * 页面的初始数据
     */
    data: {
        time: 0,
        activityInfo: {},
        join: false,
        win: false,
        open: false,
        activityCode: "timePrize",
        fontSize: 66,
        animationData: {},
        visible: false,
        num: 0,
        openid: "",
        imgPath: baseUrl + "/wedding.png",
        isImageValid: false,
        defaultAvatarUrl: 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
        userInfoVisible: false,
        tempAvatarUrl: '',
        tempNickName: '',
        isUserInfoReady: false
    },

    /**
     * 生命周期函数--监听页面加载
     */
    checkImageExists() {
      wx.getImageInfo({
        src: this.data.imgPath,
        success: () => {
          console.log("图片有效");
          this.setData({ isImageValid: true });
        },
        fail: (err) => {
          console.error("图片无效或路径错误", err);
          this.setData({ isImageValid: false });
        },
      });
    },
    onLoad() {
        this.checkImageExists();
        this.scaleAnimation()
        api.getActivityPrizes({ code: this.data.activityCode }).then((res) => {
            let dateEnd = new Date(res.data.activityEndDate)
            let nowDate = new Date()

            this.setData({
                activityInfo: res.data,
                time: dateEnd.getTime() - nowDate.getTime() + 3000
            })
            requestByOpenid(() => {
                this.setData({
                    openid: app.globalData.openid
                })
                this.getStatus()
            })
        })
    },

    getStatus() {
        wx.showLoading({
            title: "加载中…",
            mask: true
        })
        api.getActivityStatus({ code: this.data.activityCode }).then((resStatus) => {
            console.log(resStatus);

            if (resStatus.code == 0) {
                this.setData({
                    join: false
                })
            } else if (resStatus.code == 1) {
                this.setData({
                    join: true,
                    open: false
                })
            } else if (resStatus.code == 2) {
                // 中奖
                this.setData({
                    join: true,
                    win: true,
                    open: true,
                    prizeName: resStatus.data.prizeName
                })
            } else if (resStatus.code == 3) {
                // 中奖
                this.setData({
                    join: true,
                    win: false,
                    open: true
                })
            }
        }).finally(() => {
            wx.hideLoading()
        })
    },

    joinActivity() {
        console.log(app.globalData.userInfo.nickName);
        if (!!app.globalData.userInfo.nickName && app.globalData.userInfo.nickName!= '微信用户') {
            this.joinActivityApi()
            return;
        }else{
            wx.showToast({
              title: '请完善头像和昵称',
              icon: 'none'
            });
            return;
        }
        
    },

    joinActivityApi() {
        requestByOpenid(() => {
            api.joinActivity({ code: this.data.activityCode }).then((res) => {
                if (res.code == 200) {
                    wx.showToast({
                        title: "参与成功~"
                    })
                    this.setData({
                        visible: true
                    })
                    this.getStatus()
                } else {
                    wx.showToast({
                        title: res.msg,
                        icon: "error"
                    })
                }
            })
        })
    },

    finishActivity() {
        this.setData({
            visible: true
        })
        this.getStatus()
    },

    onVisibleChange(e: any) {
        this.setData({
            visible: e.detail.visible,
        });
    },

    onClose() {
        this.setData({
            visible: false,
        })
    },

    showStatus() {
        this.setData({
            visible: true
        })
    },

    toManage() {
        this.setData({
            num: this.data.num + 1
        })
        setTimeout(() => {
            this.setData({
                num: 0
            })
        }, 1000);
        if (this.data.num >= 5) {
            wx.navigateTo({
                url: "../mine/mine"
            })
        }
    },

    // 显示用户信息弹窗
  showUserInfoPopup() {
    // 如果已有用户信息，直接参与
    if (app.globalData.userInfo?.nickName && app.globalData.userInfo.nickName!= '微信用户') {
      this.joinActivityApi();
      return;
    }
    
    this.setData({
      userInfoVisible: true,
      tempAvatarUrl: app.globalData.userInfo?.avatarUrl || '',
      tempNickName: app.globalData.userInfo?.nickName && app.globalData.userInfo.nickName!= '微信用户' || ''
    });
  },

  // 关闭用户信息弹窗
  closeUserInfoPopup() {
    this.setData({
      userInfoVisible: false
    });
  },

  // 选择头像回调
  onChooseAvatar(e: any) {
    const { avatarUrl } = e.detail;
    wx.uploadFile({
      url: baseUrl + "/file/common/upload",
      filePath: avatarUrl,
      name: 'file',
      success: (res) => {
          const data = JSON.parse(res.data);
          console.log(data.data);
          this.setData({
            tempAvatarUrl:baseUrl+"/file/fetch/" + data.data.fileId
          });
      }
  })
    this.setData({
      tempAvatarUrl: avatarUrl
    });

    this.checkUserInfoReady();
  },

  // 昵称输入回调
  onNickNameInput(e: any) {
    this.setData({
      tempNickName: e.detail.value
    });
    this.checkUserInfoReady();
  },

  // 检查用户信息是否完整
  checkUserInfoReady() {
    const isReady = !!this.data.tempAvatarUrl && !!this.data.tempNickName && this.data.tempNickName!= '微信用户';
    this.setData({ isUserInfoReady: isReady });
  },

  // 提交用户信息
  submitUserInfo() {
    if (!this.data.isUserInfoReady) {
      wx.showToast({
        title: '请完善头像和昵称',
        icon: 'none'
      });
      return;
    }

    // 更新全局用户信息
    const userInfo = {
      avatarUrl: this.data.tempAvatarUrl,
      nickName: this.data.tempNickName
    };
    app.globalData.userInfo = userInfo;

    // 更新到后台
    wx.showLoading({ title: '更新信息中...' });
    api.updateUserInfo(userInfo).then(() => {
      this.setData({ userInfoVisible: false });
      // 执行后续参与逻辑
      this.joinActivityApi();
    }).catch(err => {
      wx.showToast({
        title: '更新失败',
        icon: 'error'
      });
    }).finally(() => {
      wx.hideLoading();
    });
  },

    /**
     * 生命周期函数--监听页面初次渲染完成
     */
    onReady() {

    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
    },

    /**
     * 生命周期函数--监听页面隐藏
     */
    onHide() {

    },

    /**
     * 生命周期函数--监听页面卸载
     */
    onUnload() {

    },

    /**
     * 页面相关事件处理函数--监听用户下拉动作
     */
    onPullDownRefresh() {

    },

    /**
     * 页面上拉触底事件的处理函数
     */
    onReachBottom() {

    },

    /**
     * 用户点击右上角分享
     */
    onShareAppMessage() {

    },

    // 定义循环动画函数
    scaleAnimation: function () {
        this.anni()
        setInterval(() => {
            this.anni()
        }, 2000)
    },

    anni: function () {
        const animation = wx.createAnimation({
            duration: 1000,
            timingFunction: 'linear'
        })
        animation.scale(1.2).step()
        setTimeout(() => {
            animation.scale(1).step()
            this.setData({
                animationData: animation.export(),
                fontSize: this.data.fontSize
            })
        }, 1000)

        this.setData({
            animationData: animation.export(),
            fontSize: this.data.fontSize
        })
    }
})