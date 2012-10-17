#!/bin/bash

echo making salt minion a service
systemctl enable salt-minion.service

echo starting salt minion
systemctl start salt-minion.service

