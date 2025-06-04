import base from "../../../utils/base";
import api from "../../../utils/manage/activityPrizeApiManage";
Page({
  data: {
    prizeName: '',
    count: '',
    filePath: '',
    fileId: ''
  },
  // 输入绑定（t-input 的 bind:change）
  onPrizeNameInput(e) {
    this.setData({ prizeName: e.detail.value });
  },
  onCountInput(e) {
    this.setData({ count: e.detail.value });
  },
  // 文件上传回调（t-upload 的 bind:change）
  handleSuccess(e: any) {
    const { files } = e.detail;
    console.log(files);
    for (const item of files) {
        wx.uploadFile({
            url: base + "/file/common/upload",
            filePath: item.url,
            name: 'file',
            success: (res) => {
                const data = JSON.parse(res.data);
                console.log();
                this.setData({
                    originFiles: files,
                    fileId: data.data.fileId
                });
              
                //this.refresh()
            }
        })
    }
},
refresh() {
  
},
handleRemove(e: any) {
    
},
handleClick(e: any) {
    console.log(e.detail.file);
},
  // 取消按钮
  onCancel() {
    wx.navigateBack();
  },
  // 提交按钮
  onSubmit() {
    const { prizeName, count, filePath,fileId } = this.data;
    console.log(this.data);
    console.log(prizeName);
    console.log(count);
    console.log(filePath);
    if (!prizeName || !count || !fileId) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' });
      return;
    }
    
    const add = {
      "prizeName": prizeName,
      "count": count,
      "fileId": fileId         
    }
    console.log(add);
    api.addActivityPrize(add);

    const pages = getCurrentPages();
    const prevPage = pages[pages.length - 2];
    prevPage.onActivityPrizeAddSubmit({ prizeName, count, filePath });
    wx.navigateBack();
  }
});

