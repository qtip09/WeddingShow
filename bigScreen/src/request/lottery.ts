import axios from './request'

namespace Lottery {

    export interface Prize {
        id: number;
        prizeName: string;
        filePath?: string;
        count: number;
    }
    
    export interface Participant {
        id: number;
        nickName: string
        avatarUrl:string
    }
    
    export interface Winner {
        userName: string;
        avatarUrl: string
        prizeName: string;
        prizeNum: number;
    }
}

// 获取奖品列表
export const getPrizes = () => {
    return axios.get<Lottery.Prize[]>('/bigscreen/getActivityPrizeList',{code:"timePrize"});
}

// 获取参与者列表
export const getParticipants = () => {
    return axios.get<Lottery.Participant[]>('/bigscreen/getJoinUserList',{code:"timePrize"});
}

// 执行抽奖
export const drawLottery = (prizeId?: number) => {
    return axios.get<Lottery.Winner>('/bigscreen/winPrize', { code:"timePrize",prizeId:prizeId });
}