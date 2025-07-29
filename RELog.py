import re
from typing import List, Optional

class RELog:
    _instance = None  # Singleton

    def __init__(self):
        self.regex_pattern = r"(T0)?(T1(?!1|0))?((T2)?(T3)?(T4)?|(T5)?(T6)?|(T7)?(T8)?(T9)?(T10)?)(T11)"
        self.pattern = re.compile(self.regex_pattern)
        self.input_log: Optional[str] = None
        self.matcher: Optional[re.Match] = None

    @classmethod
    def get_instance(cls):
        if cls._instance is None:
            cls._instance = RELog()
        return cls._instance

    def set_log(self, log: str):
        self.input_log = log
        self.matcher = self.pattern.search(self.input_log)

    def set_pattern(self, regex_pattern: str):
        self.pattern = re.compile(regex_pattern)
        if self.input_log is not None:
            self.matcher = self.pattern.search(self.input_log)

    def find(self) -> bool:
        self._matcher_verification()
        self.matcher = self.pattern.search(self.input_log, self.matcher.end() if self.matcher else 0)
        return self.matcher is not None

    def reset(self):
        self._matcher_verification()
        self.matcher = self.pattern.search(self.input_log)

    def get_group(self, group: int = 0) -> str:
        self._matcher_verification()
        return self.matcher.group(group)

    def get_all_groups(self) -> List[str]:
        self._matcher_verification()
        return list(self.matcher.groups(0)) if self.matcher else []

    def replace_all(self, replacement: str) -> str:
        self._pattern_verification()
        return self.pattern.sub(replacement, self.input_log)

    def replace_first(self, replacement: str) -> str:
        self._pattern_verification()
        self._input_verification()
        return self.pattern.sub(replacement, self.input_log, count=1)

    def replace_sequence(self):
        self._pattern_verification()
        self._input_verification()
        self.matcher = self.pattern.search(self.input_log)
        count = 1

        print(f"Log obtenido tras la ejecución: {self.input_log}\n")

        if not self.input_log:
            print("No se encontraron coincidencias.")
            return

        while self.matcher:
            match_text = self.matcher.group()
            self.input_log = self.replace_first("")
            print(f"Reemplazo {count}:")
            print(f"Secuencia encontrada: {match_text} Residuos: {self.input_log}\n")
            count += 1

            self.matcher = self.pattern.search(self.input_log)

        print("No se encontraron nuevas coincidencias.\n")
        self._validate_system()

    def _matcher_verification(self):
        if self.matcher is None:
            raise ValueError("Pattern o input no inicializados.")

    def _pattern_verification(self):
        if self.pattern is None:
            raise ValueError("Debe inicializar pattern antes de reemplazar.")

    def _input_verification(self):
        if self.input_log is None:
            raise ValueError("Debe establecer un log antes de buscar y/o reemplazar.")

    def _validate_system(self):
        if self.input_log:
            print(f"El sistema no ha funcionado correctamente, quedan residuos en el log: {self.input_log}\n")
        else:
            print("El sistema ha funcionado correctamente, no quedan residuos en el log. \n")

    def __str__(self):
        return f"RELog(input='{self.input_log}', pattern='{self.pattern.pattern if self.pattern else None}')"
