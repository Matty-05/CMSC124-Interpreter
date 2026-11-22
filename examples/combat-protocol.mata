# Run a shielded hit followed by normal hits in a YoRHa training simulation.
protocol makeCounter() {
    unit count = 0
    protocol nextHit() {
        count = count + 1
        transmit count
    }
    transmit nextHit
}

protocol damageFor(hit) {
    if (hit == 1) {
        unit shieldedDamage = 20
        unit armor = 5
        transmit shieldedDamage - armor
    }
    transmit 30
}

protocol simulate(label, hits) {
    unit hp = 100
    unit nextHit = makeCounter()
    report label

    for (unit i = 0, i < hits, i = i + 1) {
        unit hit = nextHit()
        hp = hp - damageFor(hit)
        report "Hit:"
        report hit
        report "Remaining HP:"
        report hp
    }
}

simulate("Primary trial", 2)
simulate("Replay trial", 1)
