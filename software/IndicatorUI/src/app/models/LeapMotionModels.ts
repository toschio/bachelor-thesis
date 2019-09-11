import {LongSummaryStatistics} from './MdlModels';

interface AssessmentXApiAgent {
  name: string;
  mbox: string;
}

interface AssessmentXApiVerb {
  id: string;
}

export interface AssessmentStatement {
  id: string;
  actor: AssessmentXApiAgent;
  verb: AssessmentXApiVerb;
}

export interface AssessmentLeapMotion {
  userID: string;
  circles: number;
  keyTaps: number;
  swipes: number;
  screenTaps: number;

  circleDuration: LongSummaryStatistics;
  keyTapDuration: LongSummaryStatistics;
  swipeDuration: LongSummaryStatistics;
  screenTapDuration: LongSummaryStatistics;

  assessment: AssessmentStatement;
}
