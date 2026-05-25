package com.euchre.platform.domain;

public enum Team {
    TEAM_A,
    TEAM_B;

    public static Team forSeat(int seat) {
        return seat == 0 || seat == 2 ? TEAM_A : TEAM_B;
    }

    public Team other() {
        return this == TEAM_A ? TEAM_B : TEAM_A;
    }
}
