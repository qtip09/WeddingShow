/**
 * 获取小程序版本信息
 * 值有：develop(开发版)、trial(体验版)、release(正式版)
*/
const accountInfo = wx.getAccountInfoSync()
const envVersion = accountInfo.miniProgram.envVersion || 'release'

const GDEnvs = {
    develop: 'https://weddingshow.cpolar.top',
    trial: 'https://weddingshow.cpolar.top',
    release: 'https://weddingshow.cpolar.top',
}

export default GDEnvs[envVersion]
