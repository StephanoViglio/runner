package cmd

import (
	"encoding/json"
	"os"
	"path/filepath"
)

type SimuladorState struct {
	PID     int    `json:"pid"`
	Port    int    `json:"port"`
	Version string `json:"version"`
}

func hubsaudeDir() string {
	home, _ := os.UserHomeDir()
	return filepath.Join(home, ".hubsaude")
}

func stateFilePath() string {
	return filepath.Join(hubsaudeDir(), "simulador-state.json")
}

func lerState() (*SimuladorState, error) {
	data, err := os.ReadFile(stateFilePath())
	if err != nil {
		return nil, err
	}
	var state SimuladorState
	if err := json.Unmarshal(data, &state); err != nil {
		return nil, err
	}
	return &state, nil
}

func salvarState(state *SimuladorState) error {
	if err := os.MkdirAll(hubsaudeDir(), 0755); err != nil {
		return err
	}
	data, err := json.MarshalIndent(state, "", "  ")
	if err != nil {
		return err
	}
	return os.WriteFile(stateFilePath(), data, 0644)
}

func limparState() error {
	return os.Remove(stateFilePath())
}