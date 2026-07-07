ALTER TABLE travel_tendencies
    ADD CONSTRAINT chk_travel_tendencies_rhythm_score CHECK (rhythm_score BETWEEN 0.0 AND 10.0),
    ADD CONSTRAINT chk_travel_tendencies_energy_score CHECK (energy_score BETWEEN 0.0 AND 10.0),
    ADD CONSTRAINT chk_travel_tendencies_consumption_score CHECK (consumption_score BETWEEN 0.0 AND 10.0),
    ADD CONSTRAINT chk_travel_tendencies_decision_score CHECK (decision_score BETWEEN 0.0 AND 10.0);
