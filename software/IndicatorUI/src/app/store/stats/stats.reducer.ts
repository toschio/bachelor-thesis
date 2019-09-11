import { createReducer, on, Action } from '@ngrx/store';
import * as _ from 'lodash';
import {Stat} from '../../models/MdlModels';
import * as StatActionsTypes from './stats.actions';
import {StatActionsUnion, StatsActionTypes} from './stats.actions';

export interface StatsState {
  stats: Stat[];
}

const initialStatsState: StatsState = {stats: []} as StatsState;


export function statsReducer(state: StatsState = initialStatsState, action: StatActionsUnion): StatsState {
  const newState: StatsState = _.cloneDeep(state);

  switch (action.type) {
    case StatsActionTypes.GET_STAT: // load data from backend
      return newState;
    case StatsActionTypes.ADD_STAT: // add current data to store
      // replace existing stats for user or add to array of all stats
      _.forEach(action.stat, (stat: Stat) => {
        const index = _.findIndex(newState.stats, (s: Stat) => s.userID === stat.userID);
        if (index < 0) {
          newState.stats.push(stat);
        }
        newState.stats[index] = stat;
      });
      return newState;
    default:
      return newState;
  }
}
