protocol ready() { transmit "ready" }
report ready() # expect: ready
report ready # expect: <protocol ready>
