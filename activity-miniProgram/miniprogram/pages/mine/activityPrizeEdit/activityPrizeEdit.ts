// activityPrizeEdit.ts
import base from "../../../utils/base";
import api from "../../../utils/manage/activityPrizeApiManage";

// 定义奖品数据类型
interface PrizeInfo {
  prizeId: number | null;
  prizeName: string;
  count: string;
  filePath: string;
  fileId: string;
}

// 定义页面数据类型
interface PageData {
  prizeInfo: PrizeInfo;
  originFiles: Array<{ url: string }>;
  gridConfig: {
    column: number;
    spacing: number;
  };
  isLoading: boolean;
}

// 定义页面方法类型
interface PageMethods {
  onLoad(options: { id: string }): void;
  fetchPrizeDetail(prizeId: string): void;
  onPrizeNameInput(e: WechatMiniprogram.CustomEvent): void;
  onCountInput(e: WechatMiniprogram.CustomEvent): void;
  handleSuccess(e: WechatMiniprogram.CustomEvent): void;
  handleRemove(e: WechatMiniprogram.CustomEvent): void;
  handleClick(e: WechatMiniprogram.CustomEvent): void;
  onCancel(): void;
  onSubmit(): void;
}

// 页面配置
Page<PageData, PageMethods>({
  data: {
    prizeInfo: {
      prizeId: null,
      prizeName: '',
      count: '',
      filePath: '',
      fileId: ''
    },
    originFiles: [],
    gridConfig: {
      column: 4,
      spacing: 12
    },
    isLoading: false
  },

  onLoad(options) {
    const prizeId = options.id;
    if (!prizeId) {
      wx.showToast({ title: '缺少奖项ID', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }
    
    this.setData({ 'prizeInfo.prizeId': parseInt(prizeId) });
    this.fetchPrizeDetail(prizeId);
  },

  // 从页面栈获取奖品详情
  fetchPrizeDetail(prizeId: string) {
    try {
      // 获取当前页面栈
      const pages = getCurrentPages();
      const prevPage = pages[pages.length - 2];
      
      // 从页面栈中获取奖品列表
      const prizeList = prevPage.data.list || [];
      
      // 查找对应奖项数据
      const prizeData = prizeList.find((item: any) => item.id === parseInt(prizeId));
      
      if (!prizeData) {
        throw new Error('未找到奖项数据');
      }
      
      // 初始化表单数据
      this.setData({
        prizeInfo: {
          prizeId: prizeData.id,
          prizeName: prizeData.prizeName,
          count: String(prizeData.count),
          filePath: prizeData.filePath,
          fileId: prizeData.fileId
        },
        originFiles: prizeData.filePath ? [{ url: prizeData.filePath }] : []
      });
      
    } catch (err) {
      console.error('获取奖品详情失败:', err);
      wx.showToast({ title: '获取奖项信息失败', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
    }
  },

  // 输入绑定
  onPrizeNameInput(e) {
    this.setData({ 'prizeInfo.prizeName': e.detail.value });
  },
  
  onCountInput(e) {
    // 限制只能输入数字
    const value = e.detail.value.replace(/[^0-9]/g, '');
    this.setData({ 'prizeInfo.count': value });
  },

  // 文件上传回调
  handleSuccess(e) {
    const { files } = e.detail;
    if (!files || files.length === 0) return;
    
    wx.showLoading({ title: '上传中...' });
    
    // 只处理第一张图片
    const file = files[0];
    wx.uploadFile({
      url: base + "/file/common/upload",
      filePath: file.url,
      name: 'file',
      success: (res) => {
        try {
          const data = JSON.parse(res.data);
          if (data.code === 200 && data.data.fileId) {
            this.setData({
              originFiles: [file],
              'prizeInfo.fileId': data.data.fileId,
              'prizeInfo.filePath': file.url
            });
            wx.showToast({ title: '上传成功', icon: 'success' });
          } else {
            wx.showToast({ title: '上传失败', icon: 'none' });
          }
        } catch (err) {
          wx.showToast({ title: '解析响应失败', icon: 'none' });
          console.error('解析上传响应失败:', err);
        }
      },
      fail: (err) => {
        wx.showToast({ title: '网络错误，请重试', icon: 'none' });
        console.error('上传文件失败:', err);
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  handleRemove(e) {
    this.setData({
      originFiles: [],
      'prizeInfo.fileId': '',
      'prizeInfo.filePath': ''
    });
  },

  handleClick(e) {
    console.log('点击图片:', e.detail.file);
  },

  // 取消按钮
  onCancel() {
    wx.navigateBack();
  },

  // 提交按钮
  onSubmit() {
    if (this.data.isLoading) return;
    
    const { prizeId, prizeName, count, fileId } = this.data.prizeInfo;
    
    // 表单验证
    if (!prizeName.trim()) {
      wx.showToast({ title: '请输入奖项名称', icon: 'none' });
      return;
    }
    
    if (!count || isNaN(Number(count)) || Number(count) <= 0) {
      wx.showToast({ title: '请输入有效的数量', icon: 'none' });
      return;
    }
    
    if (!fileId) {
      wx.showToast({ title: '请上传图片', icon: 'none' });
      return;
    }
    
    this.setData({ isLoading: true });
    wx.showLoading({ title: '提交中...' });
    
    const updateData = {
      prizeId,
      prizeName: prizeName.trim(),
      count: Number(count),
      fileId
    };
    
    // 调用API更新奖项
    api.editActivityPrize(updateData).then(res => {
      if (res.code === 200) {
        wx.showToast({ title: '更新成功', icon: 'success' });
        
        // 返回上一页并携带更新后的数据
        const pages = getCurrentPages();
        const prevPage = pages[pages.length - 2];
        
        // 触发上一页的刷新方法
        if (typeof prevPage.refreshPrizeList === 'function') {
          prevPage.refreshPrizeList();
          console.log("刷新页面");
        } else {
          // 如果没有刷新方法，则直接更新列表
          if (prevPage.data.list && prevPage.data.list.length) {
            const updatedList = prevPage.data.list.map((item: any) => {
              if (item.id === prizeId) {
                return { ...item, ...updateData, id: prizeId }; // 保持id字段一致性
              }
              return item;
            });
            
            prevPage.setData({ list: updatedList });
          }
        }
        
        // 返回上一页
        setTimeout(() => wx.navigateBack(), 1500);
      } else {
        wx.showToast({ title: res.message || '更新失败', icon: 'none' });
      }
    }).catch(err => {
      wx.showToast({ title: '网络错误，请重试', icon: 'none' });
      console.error('更新奖项失败:', err);
    }).finally(() => {
      this.setData({ isLoading: false });
      wx.hideLoading();
    });
  }
});