package org.ananas.runner.model.core;

import lombok.Data;

import java.util.LinkedList;
import java.util.UUID;

@Data
public class Pipeline {
	public String id;
	public String projectId;
	public String name;
	public String description;
	public LinkedList<Step> steps;

	public static Pipeline of() {
		Pipeline p = new Pipeline();
		p.id = UUID.randomUUID().toString();
		p.steps = new LinkedList<>();
		return p;
	}

	public int add(Iterable<Step> ss) {
		for (Step s : ss) {
			this.steps.add(s);
		}
		return this.steps.size();
	}

	public int add(Step step) {
		this.steps.add(step);
		return this.steps.size();
	}

	public Step getLastStep() {
		return this.steps.getLast();
	}

}
