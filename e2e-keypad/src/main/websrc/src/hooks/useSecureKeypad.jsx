"use client";

import {useMemo, useState} from 'react';
import axios from "axios";
import {JSEncrypt} from "jsencrypt";

export default function useSecureKeypad() {
  const [keypad, setKeypad] = useState(null);
  const [userInput, setUserInput] = useState(null);

  const getSecureKeypad = () => {}

  const onKeyPressed = (row, col) => {}

  const sendUserInput = () => {}

  return {
    states: {
      keypad,
      userInput,
    },
    actions: {
      getSecureKeypad,
      onKeyPressed,
      sendUserInput
    }
  }
}
