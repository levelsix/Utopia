package com.lvl6.utils;

import java.util.ArrayList;
import java.util.List;

import com.lvl6.info.Quest;

public class QuestGraph {
  private class Vertex {
    private int nodeId;
    private List<Integer> requiredVertices;

    public Vertex(int id, List<Integer> required, List<Integer> dependent) {
      nodeId = id;
      requiredVertices = required;
    }

    public int getNodeId() {
      return nodeId;
    }

    public List<Integer> getRequiredVertices() {
      return requiredVertices;
    }

    public String toString() {
      String s = nodeId+": ";
      for (int x : requiredVertices) {
        s += x+", ";
      }
      return "{"+s.substring(0, s.length()-2)+"}";
    }
  }

  private ArrayList<Vertex> questVertices;

  public QuestGraph(List<Quest> quests) {
    questVertices = new ArrayList<Vertex>(quests.size());
    for (Quest quest : quests) {
      questVertices.add(new Vertex(quest.getId(), quest.getQuestsRequiredForThis(), quest.getQuestsDependentOnThis()));
    }
  }

  public List<Integer> getQuestsAvailable(List<Integer> completed, List<Integer> inProgress) {
    ArrayList<Integer> available = new ArrayList<Integer>();

    for (Vertex v : questVertices) {
      if (completed == null || !completed.contains(v.getNodeId())) {
        if (inProgress == null || !inProgress.contains(v.getNodeId())) {
          if (v.getRequiredVertices().isEmpty() || 
              (completed != null && completed.containsAll(v.getRequiredVertices()))) {
            available.add(v.getNodeId());
          }
        }
      }
    }

    return available;
  }

  public String toString() {
    return questVertices.toString();
  }
}
