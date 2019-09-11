import { createReducer, on, Action } from '@ngrx/store';
import * as _ from 'lodash';
import {AssessmentLeapMotion} from '../../models/LeapMotionModels';
import {LeapActionsUnion, LeapActionTypes} from './leap-motion.actions';

export interface LeapMotionStats {
  assessmentLeapMotions: AssessmentLeapMotion[];
}

const initialStatsState: LeapMotionStats = {assessmentLeapMotions: []} as LeapMotionStats;


export function leapMotionReducer(state: LeapMotionStats = initialStatsState, action: LeapActionsUnion): LeapMotionStats {
  const newState: LeapMotionStats = _.cloneDeep(state);

  switch (action.type) {
    case LeapActionTypes.GET_LEAP: // load data from backend
      return newState;
    case LeapActionTypes.ADD_LEAP: // add current data to store
      // replace existing stats for user or add to array of all stats
      _.forEach(action.leap, (stat: AssessmentLeapMotion) => {
        const index = _.findIndex(newState.assessmentLeapMotions, (s: AssessmentLeapMotion) => s.userID === stat.userID);
        if (index < 0) {
          newState.assessmentLeapMotions.push(stat);
        } else {
          newState.assessmentLeapMotions[index] = stat;
        }
      });
      return newState;
    default:
      return newState;
  }
}
