#!/usr/bin/env python3
"""
Seeds realistic demo data into iOS simulator defaults for App Store screenshots.
Supports two modes:
  --mode=standard (Default): Downward healthy trend, 64.2 kg, -0.4 kg diff, 19-day streak, full strength stats, cycle Day 22
  --mode=doms: Trigger DOMS inflammatory fluid shield (+0.6 kg spike after leg day)
"""

import sys
import json
import time
import datetime
import subprocess

SIMULATOR_UUID = "9B1B5F93-7D08-474B-8BEA-0975F5FA751B"
BUNDLE_ID = "com.nesli.kilo"

def run_simctl(*args):
    cmd = ["xcrun", "simctl"] + list(args)
    res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    if res.returncode != 0:
        print(f"Error running simctl: {res.stderr}")
    return res.stdout

def write_default(key, value, val_type="string"):
    if val_type == "bool":
        val_arg = ["-bool", "YES" if value else "NO"]
    elif val_type == "int":
        val_arg = ["-int", str(value)]
    elif val_type == "float":
        val_arg = ["-float", str(value)]
    else:
        val_arg = ["-string", str(value)]
    
    cmd = ["xcrun", "simctl", "spawn", SIMULATOR_UUID, "defaults", "write", BUNDLE_ID, key] + val_arg
    subprocess.run(cmd, check=True)

def seed_data(mode="standard"):
    print(f"Seeding demo data for mode: {mode}...")
    
    # Base timestamp today (2026-10-16 noon)
    now = datetime.datetime.now()
    today_date = now.date()
    epoch_base = datetime.date(1970, 1, 1)
    today_epoch_days = (today_date - epoch_base).days
    
    # 1. Generate weight history (past 20 days)
    # Weights moving gradually from 65.8 down to 64.2 kg
    weights_progression = [
        65.8, 65.7, 65.5, 65.6, 65.3, 65.4, 65.1, 65.0,
        64.9, 65.1, 64.8, 64.7, 64.6, 64.5, 64.4, 64.3,
        64.1, 64.0
    ]
    
    if mode == "doms":
        # Spikes up on latest day due to DOMS
        weights_progression.extend([64.0, 64.6])
    else:
        # Standard: 64.6 yesterday, 64.2 today (-0.4 kg diff)
        weights_progression.extend([64.6, 64.2])
    
    entries = []
    n_days = len(weights_progression)
    for i, w in enumerate(weights_progression):
        # Day offset: from -(n_days - 1 - i) to 0
        day_offset = -(n_days - 1 - i)
        entry_date = today_date + datetime.timedelta(days=day_offset)
        # Weigh-in at 07:30 AM
        entry_dt = datetime.datetime.combine(entry_date, datetime.time(7, 30, 0))
        at_millis = int(entry_dt.timestamp() * 1000)
        
        tag = None
        if i % 4 == 0:
            tag = "morning"
        elif i % 5 == 0:
            tag = "fasted"
            
        entries.append({
            "id": f"seed-entry-{i+1}",
            "kg": round(w, 2),
            "atMillis": at_millis,
            "tag": tag
        })
        
    entries_json = json.dumps(entries)
    write_default("weight_entries_v1", entries_json)
    
    # 2. Preferences
    write_default("weight_unit", "KG")
    write_default("health_sync_enabled", True, "bool")
    write_default("health_onboarding_seen", True, "bool")
    write_default("goal_kg", "62.0")
    write_default("goal_start_kg", "66.0")
    write_default("reminder_enabled", True, "bool")
    write_default("reminder_minutes", 480, "int") # 08:00
    
    # Cycle: Female, Day 22 of 28 (Luteal phase)
    # If today is Day 22, period started 21 days ago
    last_period_date = today_date - datetime.timedelta(days=21)
    write_default("cycle_enabled", True, "bool")
    write_default("cycle_last_period_date", last_period_date.isoformat())
    write_default("cycle_length", 28, "int")
    write_default("cycle_period_duration", 5, "int")
    write_default("user_gender_v1", "FEMALE")
    write_default("cycle_dismissed_v1", False, "bool")
    
    # 3. Workouts (HevyWorkoutSession)
    # Legs session 1 day ago (volume 8,450 kg -> satisfies DOMS requirement)
    leg_day_date = today_date - datetime.timedelta(days=1)
    leg_epoch_days = (leg_day_date - epoch_base).days
    leg_start_ms = int(datetime.datetime.combine(leg_day_date, datetime.time(18, 0)).timestamp() * 1000)
    leg_end_ms = leg_start_ms + 65 * 60 * 1000
    
    workouts = [
        {
            "id": "leg-day-hypertrophy",
            "title": "Lower Body Strength",
            "dateEpochDays": leg_epoch_days,
            "startTimeEpochMs": leg_start_ms,
            "endTimeEpochMs": leg_end_ms,
            "durationMinutes": 65,
            "totalVolumeKg": 8450.0,
            "exercises": [
                {
                    "title": "Barbell Squat",
                    "category": "LEGS_GLUTES",
                    "sets": [
                        {"index": 1, "type": "normal", "weightKg": 75.0, "reps": 10},
                        {"index": 2, "type": "normal", "weightKg": 85.0, "reps": 8},
                        {"index": 3, "type": "normal", "weightKg": 90.0, "reps": 6}
                    ],
                    "totalVolumeKg": 1970.0,
                    "maxWeightKg": 90.0,
                    "estimated1RmKg": 108.0
                },
                {
                    "title": "Barbell Hip Thrust",
                    "category": "LEGS_GLUTES",
                    "sets": [
                        {"index": 1, "type": "normal", "weightKg": 100.0, "reps": 12},
                        {"index": 2, "type": "normal", "weightKg": 115.0, "reps": 10},
                        {"index": 3, "type": "normal", "weightKg": 125.0, "reps": 8}
                    ],
                    "totalVolumeKg": 3350.0,
                    "maxWeightKg": 125.0,
                    "estimated1RmKg": 158.3
                },
                {
                    "title": "Barbell Deadlift",
                    "category": "BACK_PULL",
                    "sets": [
                        {"index": 1, "type": "normal", "weightKg": 95.0, "reps": 6},
                        {"index": 2, "type": "normal", "weightKg": 105.0, "reps": 5}
                    ],
                    "totalVolumeKg": 1095.0,
                    "maxWeightKg": 105.0,
                    "estimated1RmKg": 122.5
                },
                {
                    "title": "Barbell Bench Press",
                    "category": "CHEST_PUSH",
                    "sets": [
                        {"index": 1, "type": "normal", "weightKg": 55.0, "reps": 8},
                        {"index": 2, "type": "normal", "weightKg": 65.0, "reps": 6}
                    ],
                    "totalVolumeKg": 830.0,
                    "maxWeightKg": 65.0,
                    "estimated1RmKg": 78.0
                }
            ]
        }
    ]
    
    write_default("hevy_workouts_v1", json.dumps(workouts))
    print("Demo data successfully written to simulator preferences!")

if __name__ == "__main__":
    mode = "standard"
    if "--mode=doms" in sys.argv:
        mode = "doms"
    seed_data(mode)
