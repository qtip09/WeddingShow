import axios from './request'

namespace Lottery {
    export interface Prize {
        id: number;
        name: string;
        image?: string;
        count: number;
    }
    
    export interface Participant {
        id: number;
        name: string;
        avatar?: string;
    }
    
    export interface Winner {
        prizeId: number;
        prizeName: string;
        participantId: number;
        participantName: string;
    }
}

// 获取奖品列表
export const getPrizes = () => {
    return axios.get<Lottery.Prize[]>('/lottery/prizes');
}

// 获取参与者列表
export const getParticipants = () => {
    return axios.get<Lottery.Participant[]>('/lottery/participants');
}

// 执行抽奖
export const drawLottery = (prizeId?: number) => {
    return axios.post<Lottery.Winner>('/lottery/draw', { prizeId });
}