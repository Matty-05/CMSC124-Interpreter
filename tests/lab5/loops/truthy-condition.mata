unit hp = 2
while (hp) {
    report hp  # expect: 2
               # expect: 1
    hp = hp - 1
    if (hp == 0) hp = void
}
