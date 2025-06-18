import { httpRequest } from '../request'
import baseUrl from '../base'

export default class activityApi {

    /**
     * 获取活动奖品列表
     */
  static getActivityPrizes = (data: any) =>
    httpRequest.get<any>(
        baseUrl + '/activity/getActivityPrizeList',
        data
    )
  /**
     * 获取活动奖品列表
     */
  static addActivityPrize = (data: any) =>
  httpRequest.post<any>(
      baseUrl + '/activity/addActivityPrize',
      data
  )

    /**
     * 禁用用户
     */
  static banUserByBulletId = (data: any) =>
    httpRequest.get<any>(
      baseUrl + '/bullet/banUserByBulletId',
      data
    )
  static deletePrize = (data: any) =>
    httpRequest.get<any>(
      baseUrl + '/activity/delActivityPrize',
      data
    )

  static editActivityPrize = (data: any) =>
    httpRequest.post<any>(
      baseUrl + '/activity/editActivityPrize',
      data
    )

    static getActivityWinSpecifyList = (data: any) =>
    httpRequest.post<any>(
      baseUrl + '/activity/getActivityWinSpecifyList',
      data
    )

    static searchUsers = (data: any) =>
    httpRequest.get<any>(
      baseUrl + '/activity/searchUsers',
      data
    )
    
    static addAwardUser = (data: any) =>
    httpRequest.post<any>(
      baseUrl + '/activity/addActivityWinSpecify',
      data
    )

    static deleteAwardUser = (data: any) =>
    httpRequest.get<any>(
      baseUrl + '/activity/delActivityWinSpecify',
      data
    )

    
    
}