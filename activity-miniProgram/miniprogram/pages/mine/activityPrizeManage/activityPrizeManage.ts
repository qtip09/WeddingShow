import api from "../../../utils/manage/activityPrizeApiManage"

Page({
    /**
     * 页面的初始数据
     */
    data: {
        pageSize: 20,
        pageNum: 1,
        activityCode: "timePrize",
        list: [] as any,
        editDialogShow: false,
        addDialogShow: false,
        editPrize: {
          prizeName: '',
          count: '',
          filePath: ''
        },
        addPrize: {
          prizeName: '',
          count: '',
          filePath: '',
          fileId: ''
        },
        awardUserDialogShow: false,
        currentPrize: {
          id: 0,
          prizeName: '',
          remainingCount: 0
        },
        awardUsers: [], // 已指定用户列表
        addUserFormShow: false, // 新增表单是否显示
        newUser: {
          uid: '',
          name: '',
          openId: ''
    
        },
        selectedUserId: null,
        searchPageNum: 1,      // 搜索结果的当前页码
        searchPageSize: 10,    // 每页显示数量
        hasMoreSearch: true,   // 是否还有更多搜索结果
        isSearchLoading: false, // 是否正在加载搜索结果
        // 新增搜索相关数据
        searchKey: '',       // 搜索关键词
        searchResults: []   // 搜索结果列表
    },
    /**
     * 生命周期函数--监听页面加载
     */
    onLoad() {
        this.getPage(1, 20, true)
    },
    refreshPrizeList() {
      wx.showLoading({ title: '刷新中...' });
      
      // 重置页码并重新加载数据
      this.setData({
          pageNum: 1
      });
      
      this.getPage(1, this.data.pageSize, true);
    },
    getPage(pageNum: number, pageSize: number, flag: boolean) {
        api.getActivityPrizes({
            pageSize: pageSize,
            pageNum: pageNum,
            code: this.data.activityCode,
        }).then((res) => {
            if (res.code == 200) {
                this.setData({
                  list: res.data.activityPrizeDtoList
                })
                
            }
        }).finally(() => {
            wx.stopPullDownRefresh()
        })
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
        this.setData({
            pageNum: 1,
            pageSize: 20
        })
        this.getPage(this.data.pageNum, this.data.pageSize, true)
    },

    /**
     * 页面上拉触底事件的处理函数
     */
    onReachBottom() {
        this.setData({
            pageNum: this.data.pageNum + 1
        })
        this.getPage(this.data.pageNum, this.data.pageSize, false)
    },

    /**
     * 用户点击右上角分享
     */
    onShareAppMessage() {

    },

    onAddPrizeTap() {
      wx.navigateTo({
        url: '/pages/mine/activityPrizeAdd/activityPrizeAdd' // 修改为新的页面路径
      });
    },
    onEditPrizeTap(e) {
      const prizeId = e.currentTarget.dataset.id;
      wx.navigateTo({
        url: `/pages/mine/activityPrizeEdit/activityPrizeEdit?id=${prizeId}` // 编辑页面路径
      });
    },
    // 接收新增页面返回的数据（方法名改为 onActivityPrizeAddSubmit）
    onActivityPrizeAddSubmit(data) {
      const { prizeName, count ,fileId} = data;
      console.log('新增奖项:', prizeName, count);
      api.addActivityPrize(data).then((res) => {
        if(res.code == 200 && res.data == true){
          wx.showToast({
            title: '新增商品成功',
            icon: 'success'
          });
          this.getPage(this.data.pageNum, this.data.pageSize, true);
        }else{
          wx.showToast({
            title: '新增失败',
            icon: 'none'
          });
        }
      });

    },
    // 删除奖品
    onDeleteTap(e: any) {
      const { id, name } = e.currentTarget.dataset;
      
      // 显示确认对话框
      wx.showModal({
        title: '确认删除',
        content: `确定要删除奖品"${name}"吗？此操作不可撤销。`,
        success: (res) => {
          if (res.confirm) {
            // 用户点击确定，调用API删除奖品
            this.deletePrize(id);
          }
        }
      });
    },
    // 删除奖品
    deletePrize(prizeId: number) {
      api.deletePrize({
        activityPrizeId: prizeId
      }).then((res) => {
        if (res.code === 200 ) {
          wx.showToast({
            title: '删除成功',
            icon: 'success'
          });
          
          // 从本地列表中移除该奖品
          this.removePrizeFromList(prizeId);
        } else {
          wx.showToast({
            title: res.message || '删除失败',
            icon: 'none'
          });
        }
      }).catch(() => {
        wx.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        });
      });
    },
    
    // 从本地列表中移除奖品
    removePrizeFromList(prizeId: number) {
      const list = this.data.list.filter((item: any) => item.id !== prizeId);
      this.setData({ list });
    },
    // 打开弹窗（从列表按钮触发）
  onAssignUserTap(e) {
    const prizeId = e.currentTarget.dataset.id;
    
    // 1. 获取当前奖项信息
    const currentPrize = this.data.list.find(item => item.id === prizeId);
    if (!currentPrize) return;
    
    // 2. 获取该奖项已指定的用户列表（模拟数据，实际需调用API）
    const mockAwardUsers = [
      { id: 1, userName: '张三', phoneNumber: '13800138000' },
      { id: 2, userName: '李四', phoneNumber: '13900139000' }
    ];
    
    // this.setData({
    //   awardUserDialogShow: true,
    //   currentPrize: {
    //     ...currentPrize,
    //     remainingCount: currentPrize.count - mockAwardUsers.length
    //   },
    //   awardUsers: mockAwardUsers
    // });
    
    // 3. 实际项目中应调用API获取用户列表
    this.fetchAwardUsers(currentPrize);
  },

  // 获取获奖用户列表（示例API调用）
  fetchAwardUsers(currentPrize) {
    
    api.getActivityWinSpecifyList({ 
      id:currentPrize.id,
      prizeName:currentPrize.prizeName
    }).then(res => {
      if (res.code === 200) {
        // 处理返回数据（即使为空数组也正常设置）
        this.setData({
          awardUsers: res.data || [], // 确保是数组类型
          'currentPrize.remainingCount': currentPrize.count - (res.data.length || 0) // 避免负数
        });
        // 显示弹窗（无论是否有数据都打开）
        this.setData({
          awardUserDialogShow: true,
          currentPrize: {
            ...currentPrize,
            // 补充剩余数量计算（确保不小于0）
            remainingCount: Math.max(currentPrize.count - (res.data.length || 0), 0)
          }
        });
      }
    }).catch(err => {
      console.error('获取获奖用户失败:', err);
      wx.showToast({ title: '获取用户列表失败', icon: 'none' });
    });
  },

  // 显示新增表单
  showAddUserForm() {
    console.log("点击新增");
    this.setData({
      addUserFormShow: true,
      newUser: { uid: '', name: '' }
    });
  },

  // 隐藏新增表单
  hideAddUserForm() {
    this.setData({ addUserFormShow: false });
  },


  // 删除用户
  onDeleteUser(e) {
    const userId = e.currentTarget.dataset.id;
    const activityWinSpecifyId = e.currentTarget.dataset.id;
    console.log(userId);
    wx.showModal({
      title: '确认删除',
      content: '确定要删除该获奖人员吗？',
      success: (res) => {
        if (res.confirm) {
          // 从列表中移除用户（实际需调用API）
          api.deleteAwardUser({
            "activityWinSpecifyId": activityWinSpecifyId
          }).then((res) => {
            if (res.code === 200 && res.data == true ) {
              wx.showToast({
                title: '删除成功',
                icon: 'success'
              });
              const newUsers = this.data.awardUsers.filter(item => item.id !== userId);
              this.setData({
                awardUsers: newUsers,
                'currentPrize.remainingCount': this.data.currentPrize.remainingCount + 1
              });
    
            } else {
              wx.showToast({
                title: res.msg || '删除失败',
                icon: 'none'
              });
            }
          }).catch(() => {
            wx.showToast({
              title: '网络错误，请重试',
              icon: 'none'
            });
          });

 
        }
      }
    });
  },

  // 关闭弹窗
  onCloseDialog() {
    // 仅在点击关闭按钮时关闭弹窗，点击遮罩层不关闭
    // 注意：需要在遮罩层的 bindtap 中取消事件冒泡或修改逻辑
    console.log("11")
    this.setData({
      awardUserDialogShow: false,
      addUserFormShow: false
    });
    this.hideAddUserForm();
    //this.fetchAwardUsers(this.data.currentPrize); // 刷新用户列表
  },
  
  // 新增：处理遮罩层点击事件（阻止关闭弹窗）
  onMaskTap(e) {
    // 阻止遮罩层点击关闭弹窗（如果需要完全禁用遮罩层关闭，直接返回即可）
    if (e.currentTarget === e.target) { // 确保点击的是遮罩层本身
      // 这里不执行关闭操作，实现点击遮罩层不关闭弹窗
    }
  },
  // 搜索输入处理
  onSearchInput(e) {
    this.setData({ searchKey: e.detail.value });
  },

  // 触发搜索接口
  handleSearch() {
    
    const { searchKey } = this.data;
    if (!searchKey.trim()) return;
    
    // 重置分页状态
    this.setData({
      searchPageNum: 1,
      hasMoreSearch: true,
      searchResults: []
    });
    
    this.loadSearchResults(true);
  },
  // 搜索结果滚动到底部
  onSearchScrollToLower() {
    this.loadSearchResults();
  },
  // 分页加载搜索结果
  loadSearchResults(isNewSearch = false) {
    if (this.data.isSearchLoading || !this.data.hasMoreSearch) return;
    
    this.setData({ isSearchLoading: true });
    
    api.searchUsers({ 
      name: this.data.searchKey,
      pageNum: this.data.searchPageNum,
      pageSize: this.data.searchPageSize
    }).then(res => {
      if (res.code === 200) {
        const newData = res.data.records || []; // 注意这里取 records
        const hasMore = this.data.searchPageNum < res.data.pages;
          
        this.setData({
          searchResults: isNewSearch ? newData : [...this.data.searchResults, ...newData],
          hasMoreSearch: hasMore,
          searchPageNum: this.data.searchPageNum + 1
        });
      }else {
        wx.showToast({ title: '搜索失败', icon: 'none' });
      }
    }).finally(() => {
      this.setData({ isSearchLoading: false });
    });
  },


  // 从搜索结果中选择用户
  selectUserFromSearch(e) {
    const selectedUser = e.currentTarget.dataset.user;
    console.log(selectedUser);
    this.setData({
      newUser: { uid: selectedUser.id, name: selectedUser.nickName ,openId: selectedUser.openid},
      selectedUserId: selectedUser.id // 记录选中的用户ID
    });
    console.log(this.data.newUser);
  },
  // 添加用户（修改为使用搜索结果或手动输入）
  handleAddUser() {
    console.log(this.data.newUser);
    const { name } = this.data.newUser;
    console.log(name);
    if (!name) {
      wx.showToast({ title: '请选择或输入姓名', icon: 'none' });
      return;
    }

    // 实际项目中调用API添加用户（携带用户ID或姓名）
    api.addAwardUser({
      prizeId: this.data.currentPrize.id,
      uid: this.data.newUser.uid,
      name: this.data.newUser.name
    }).then(res => {
      if (res.code === 200 && res.data == true) {
        // 添加成功逻辑
        wx.showToast({
          title: '添加成功',
          icon: 'success'
        });
        this.hideAddUserForm();
        this.fetchAwardUsers(this.data.currentPrize); // 刷新用户列表
      }else{
        wx.showToast({
          title: '添加失败',
          icon: 'none'
        });
      }
    });
  },

  // 隐藏表单时清空搜索数据
  hideAddUserForm() {
    this.setData({
      addUserFormShow: false,
      searchKey: '',
      searchResults: [],
      newUser: { userName: '' },
      selectedUserId: null
    });
  }
  
})


