package com.lvl6.info;

import java.util.List;
import com.lvl6.proto.InfoProto.DialogueProto.SpeechSegmentProto.DialogueSpeaker;

public class Dialogue {
  
  List<DialogueSpeaker> speakers;
  List<String> speakerTexts;
  
  public Dialogue(List<DialogueSpeaker> speakers, List<String> speakerTexts) {
    this.speakers = speakers;
    this.speakerTexts = speakerTexts;
  }
  
  public List<DialogueSpeaker> getSpeakers() {
    return speakers;
  }
  public List<String> getSpeakerTexts() {
    return speakerTexts;
  }

  @Override
  public String toString() {
    return "Dialogue [speakers=" + speakers + ", speakerTexts=" + speakerTexts
        + "]";
  }
  
}