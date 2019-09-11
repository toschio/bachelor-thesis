import { ActionReducerMap } from '@ngrx/store';
import {statsReducer, StatsState} from './stats/stats.reducer';
import {leapMotionReducer, LeapMotionStats} from './leap-motion/leap-motion.reducer';

export interface AppState {
  stats: StatsState;
  leapMotionStats: LeapMotionStats;
}

export const reducers: ActionReducerMap<AppState> = {
  stats: statsReducer,
  leapMotionStats: leapMotionReducer,
};

