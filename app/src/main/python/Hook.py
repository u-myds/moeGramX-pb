from abc import ABC, abstractmethod

class XposedHook(ABC):
    pass

class MethodHook(XposedHook):
    def beforeHookedMethod(self, param):
        pass
    def afterHookedMethod(self, param):
        pass

class MethodReplacement(XposedHook):
    @abstractmethod
    def replaceHookedMethod(self, param):
        pass