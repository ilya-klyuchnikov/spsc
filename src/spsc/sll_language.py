'''
Created on Aug 14, 2009

@author: Sergei Romanenko
'''

import string

class Exp(object) : pass

class Var(Exp):
    def __init__(self, name):
        self.name = name
    def __str__(self):
        return self.name

#  #  data CKind = Ctr | FCall | GCall
#  #    deriving (Eq)

class Call(Exp):
    def __init__(self, ckind, name, args):
        self.ckind = ckind
        self.name = name
        self.args = args
    def hasTheSameFunctorAs(self, other):
        return (self.__class__ is other.__class__ 
#                and self.ckind == other.ckind 
                and self.name == other.name)
    
    def __str__(self):
        args_s = ",".join(["%s" % e for e in self.args])
        return "%s(%s)" % (self.name, args_s)

class Binding(object) :
    def __init__(self, name, exp):
        self.name = name
        self.exp = exp
    def __str__(self):
        return "%s=%s" % (self.name, self.exp)

class Let(Exp):
    def __init__(self, body, bindings):
        self.body = body
        self.bindings = bindings
    def __str__(self):
        bindings_s = ",".join(["%s" % b for b in self.bindings])
        return "let %s in %s" % (bindings_s, self.body)

class Ctr(Call):
    def __init__(self, name, args):
        Call.__init__(self, "Ctr", name, args)
    def __str__(self):
        if len(self.args) == 0:
            return self.name
        else:
            return Call.__str__(self)

class FCall(Call):
    def __init__(self, name, args):
        Call.__init__(self, "FCall", name, args)

class GCall(Call):
    def __init__(self, name, args):
        Call.__init__(self, "GCall", name, args)

class FRule(object):
    def __init__(self, name, params, body):
        self.name = name
        self.params = params
        self.body = body
    def __str__(self):
        params_s = ",".join(self.params)
        body_s = "%s" % self.body
        return self.name + "(" + params_s + ")=" + body_s + ";"

class GRule(object):
    def __init__(self, name, cname, cparams, params, body):
        self.name = name
        self.cname = cname
        self.cparams = cparams
        self.params = params
        self.body = body
    def __str__(self):
        params_s = ",".join(self.params)
        cparams_s = ",".join(self.cparams)
        pat_s = self.cname
        if len(self.cparams) > 0 :
            pat_s += "(" + cparams_s + ")"
        if len(self.params) > 0:
            pat_s += ","
        body_s = "%s" % self.body
        return self.name + "(" + pat_s + params_s + ")=" + body_s + ";"

class Program(object):
    def __init__(self, rules):
        self.rules = rules
    def __str__(self):
        return "".join(["%s" % r for r in self.rules])
   
