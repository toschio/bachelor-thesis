import {AppState} from '../index';
import {createSelector} from '@ngrx/store';
import {AssessmentLeapMotion} from '../../models/LeapMotionModels';
import {LeapMotionStats} from './leap-motion.reducer';
import * as _ from 'lodash';


const getLeapMotionState = (state: AppState): LeapMotionStats => state.leapMotionStats;

const getAssessmentLeapMotion = (state: LeapMotionStats): AssessmentLeapMotion[] => state.assessmentLeapMotions;

export const selectAllLeapMotion = createSelector(getLeapMotionState, getAssessmentLeapMotion);

export const selectLeapMotion = createSelector(
  selectAllLeapMotion,
  (leaps, props) => _.find(leaps, (next: AssessmentLeapMotion) => next.userID === props.userID)
)
